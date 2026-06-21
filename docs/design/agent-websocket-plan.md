# PC Agent WebSocket 长连接改造计划

## 当前决策

PC Agent 与 Home Center 服务端的通信方式已确定为 WebSocket 长连接。Agent 主动连接服务端，服务端通过长连接下发控制命令并接收执行结果。后续实现应按本文方案推进，不再使用服务端主动连接 Agent IP 和端口的旧模型。

## 目标

将 PC Agent 与 Home Center 服务端的通信模型改为：

```text
pc-agent -> home-center server WebSocket
```

客户端主动连接服务端，并通过这条长连接接收控制命令、返回执行结果。服务端不再主动连接客户端，客户端不再监听 TCP 端口，从根上避免 NAT、防火墙、端口冲突和多实例调试问题。

本次改造直接替换旧 socket 通信方案，不做兼容保留。

## 当前问题

现有设计是服务端通过 `ip_address + socket_port` 主动连接客户端 agent：

- 客户端监听 TCP `65432`。
- 服务端调用 `SimpleSocketSender` 主动连客户端。
- `pc_device.socket_port` 保存客户端监听端口。
- `pc_device.ip_address` 是必填唯一字段。
- `pc_device.mac_address` 是必填唯一字段。

实际数据库 `home_center_dev.pc_device` 当前结构中：

```text
ip_address  NOT NULL UNIQUE
mac_address NOT NULL UNIQUE
socket_port NOT NULL DEFAULT 65432
```

这在长连接模型下不合理：

- 服务端不应依赖客户端 IP 和端口。
- 客户端 IP 可能受 DHCP、NAT、VPN、多网卡影响。
- 一台机器可能有多个 MAC，当前库中“书房台式机”已经存在两条不同网卡记录。
- `name` 也不唯一，不能作为稳定设备身份。

## 目标设计

### 连接模型

- Agent 启动后读取本地配置中的 `server.url`。
- 如果 `server.url` 是 `http://`，WebSocket 使用 `ws://`。
- 如果 `server.url` 是 `https://`，WebSocket 使用 `wss://`。
- 本地开发不需要证书。
- 生产环境使用 `wss://` 时复用服务端 HTTPS/SSL 证书。
- 不做客户端鉴权；客户端默认信任自己配置的服务端地址。
- 如果服务端使用自签证书，Java 客户端需要配置 truststore；不关闭证书校验。

建议 WebSocket endpoint：

```text
/api/agent/ws
```

### Agent 身份

新增稳定身份字段：

```text
agent_id
```

规则：

- Agent 第一次启动时生成 UUID。
- UUID 写入本地状态文件或配置文件。
- 后续每次连接服务端都携带同一个 `agentId`。
- 服务端用 `agent_id` 作为首要匹配依据。
- `agent.name`、hostname、MAC 只作为展示信息和首次绑定辅助条件，不作为唯一身份。

注册消息示例：

```json
{
  "type": "REGISTER",
  "agentId": "6d0a3a2a-82f0-4f5c-8bb4-0bb0f4c3b6b0",
  "name": "书房台式机",
  "hostname": "DESKTOP-001",
  "macAddresses": ["58:41:20:C5:8D:66", "2C:F0:5D:81:40:75"],
  "agentVersion": "1.0",
  "osName": "Windows 10"
}
```

服务端匹配顺序：

1. `agentId` 命中已有设备：更新在线连接和设备信息。
2. `agentId` 不存在，但 MAC 命中已有设备：绑定 `agentId` 到该设备。
3. 都未命中：自动注册新设备。

自动注册的新设备默认：

```text
status = 1
name = agent.name，如果为空则 hostname，如果仍为空则 agentId 前 8 位
```

该策略已确定：首次连接未匹配到已有 `agentId` 时自动创建设备记录，不进入待审核流程。自动创建设备默认启用，创建后可在后台重命名、禁用或管理。

### 数据库模型

`pc_device` 第一版目标字段：

```text
id
name
description
agent_id
ip_address
mac_address
status
create_time
update_time
last_seen_time
hostname
os_name
agent_version
```

删除：

```text
socket_port
uk_ip
uk_mac
```

调整：

```text
ip_address  改为 NULL，保留为最后连接来源 IP 或展示字段
mac_address 改为 NULL，保留为主 MAC 或 WOL 使用字段
agent_id    新增唯一索引
```

建议迁移 SQL：

```sql
ALTER TABLE pc_device
  ADD COLUMN agent_id varchar(64) NULL COMMENT 'Agent唯一ID' AFTER description,
  ADD COLUMN hostname varchar(100) NULL COMMENT '主机名' AFTER mac_address,
  ADD COLUMN os_name varchar(100) NULL COMMENT '操作系统' AFTER hostname,
  ADD COLUMN agent_version varchar(50) NULL COMMENT 'Agent版本' AFTER os_name,
  ADD COLUMN last_seen_time datetime NULL COMMENT '最后在线时间' AFTER status;

ALTER TABLE pc_device DROP INDEX uk_ip;
ALTER TABLE pc_device DROP INDEX uk_mac;

ALTER TABLE pc_device
  MODIFY COLUMN ip_address varchar(50) NULL COMMENT '最后连接IP地址',
  MODIFY COLUMN mac_address varchar(50) NULL COMMENT '主MAC地址';

ALTER TABLE pc_device DROP COLUMN socket_port;

CREATE UNIQUE INDEX uk_agent_id ON pc_device(agent_id);
```

注意：

- 迁移前需要确认现有 `pc_device` 数据是否要合并。
- 当前 `id=1` 和 `id=2` 很可能是同一台机器的不同网卡；WebSocket agent 模型下更合理的是一台机器一条设备记录。
- WOL 如果未来需要管理多网卡，建议新增 `pc_device_mac` 表；第一版先保留 `pc_device.mac_address` 作为主 MAC。

## 服务端改造

### 新增长连接管理

新增 WebSocket 处理能力：

- 增加 `spring-boot-starter-websocket` 依赖。
- 注册 `/api/agent/ws` endpoint。
- 维护内存映射：

```text
deviceId -> WebSocketSession
requestId -> CompletableFuture
```

行为：

- 同一设备重复连接时，新 session 覆盖旧 session，并关闭旧 session。
- session 关闭时清理在线映射。
- 在线状态以 session 内存状态为准。
- `last_seen_time` 持久化到数据库。

### 替换命令下发

删除或废弃：

```text
SimpleSocketSender
```

新增 WebSocket 命令发送组件，提供等价能力：

```text
sendCommand(deviceId, command)
executeCommand(deviceId, command, timeoutSeconds)
```

上层接口保持当前同步语义：

- 设备离线：立即返回明确错误。
- 命令执行超时：返回超时错误并清理 pending request。
- 命令成功：返回 agent 的执行结果。

需要替换的调用点：

- 关机：`PcPowerEventProcessor`
- 截图触发：`PcMonitorController`
- 远程命令：`PcCommandController`

### 截图流程

保持现有外部行为：

1. 前端请求服务端截图接口。
2. 服务端通过 WebSocket 下发 `screenshot` 命令。
3. Agent 截图。
4. Agent 继续通过 HTTP `/api/screenshot` 上传图片。
5. 服务端等待新截图文件出现并返回图片 URL。

### 后台管理页面

移除 PC 设备管理中的端口字段：

- 删除端口输入框。
- 删除列表中的端口列。
- 删除新增/编辑请求中的 `socketPort`。

IP 和 MAC 字段：

- 不再要求必填。
- IP 展示为“最后连接 IP”。
- MAC 展示为“主 MAC / WOL MAC”。

## 客户端改造

删除客户端监听逻辑：

- 删除 `SocketServer` 启动。
- 删除 `ClientHandler` 作为入站 socket 处理器。
- 删除基于监听端口的配置。

新增 WebSocket 客户端：

- 启动时读取 `server.url`。
- 生成或读取本地 `agentId`。
- 建立 WebSocket 连接。
- 连接成功后发送 `REGISTER`。
- 收到命令后执行本地动作并返回结果。
- 连接断开后自动重连。

命令处理继续复用现有能力：

- `shutdown`
- `screenshot`
- `check-update`
- shell 命令执行

远程命令执行后续按 Web Shell 形态设计：进入 Web Shell 前做权限校验和风险确认，进入后执行单条命令不再每次重复二次确认，但每条命令都必须记录审计日志。

## 消息协议

统一使用 JSON 文本消息。

服务端和客户端都必须携带 `requestId` 来关联请求和结果。

建议消息类型：

```text
REGISTER
REGISTERED
COMMAND
EXEC
RESULT
PING
PONG
ERROR
```

`COMMAND` 示例：

```json
{
  "type": "COMMAND",
  "requestId": "req-001",
  "command": "screenshot"
}
```

`EXEC` 示例：

```json
{
  "type": "EXEC",
  "requestId": "req-002",
  "command": "ipconfig",
  "timeoutSeconds": 30,
  "maxOutputBytes": 65536
}
```

`RESULT` 示例：

```json
{
  "type": "RESULT",
  "requestId": "req-002",
  "success": true,
  "exitCode": 0,
  "stdout": "...",
  "stderr": "",
  "timedOut": false,
  "truncated": false,
  "durationMillis": 120,
  "error": ""
}
```

## 审计日志

PC Agent 相关敏感操作必须记录审计日志。

需要审计的操作：

- 截图。
- 关机、重启等电源操作。
- 远程命令执行。
- 后续新增的高风险控制能力。

审计字段建议：

```text
id
request_id
operator_user_id
operator_username
device_id
agent_id
device_name
operation_type
command_summary
request_time
complete_time
success
failure_reason
source_ip
user_agent
duration_millis
```

规则：

- 审计日志应在服务端发起控制请求时创建。
- Agent 返回结果后更新执行结果、完成时间和失败原因。
- 设备离线、命令超时、WebSocket 断开等失败场景也要记录。
- 远程命令执行只记录命令摘要和执行结果，不记录完整命令输出。
- Web Shell 中的每条命令都必须生成审计记录。
- 后台需要提供 PC Agent 操作审计查询入口。
- 审计查询默认只允许超管或有权限的管理员访问。

## 分支计划

两个仓库使用同名功能分支：

```text
codex/agent-websocket
```

仓库：

```text
<server-repo-path>
<pc-agent-repo-path>
```

创建分支前需要先确认两个仓库未提交改动是否带入分支。

## 测试计划

服务端：

- Agent 注册时按 `agentId` 匹配已有设备。
- 没有 `agentId` 但 MAC 命中时绑定已有设备。
- 未命中时自动注册设备。
- 同一设备重复连接时新 session 替换旧 session。
- 设备离线时命令接口返回明确错误。
- 命令超时时清理 pending request。
- 后台管理页面不再展示或提交端口。

客户端：

- 首次启动生成 `agentId` 并持久化。
- 后续启动复用同一个 `agentId`。
- `server.url=http://...` 转换为 `ws://.../api/agent/ws`。
- `server.url=https://...` 转换为 `wss://.../api/agent/ws`。
- WebSocket 断开后自动重连。
- 不再启动 TCP `ServerSocket`。
- 收到 `shutdown`、`screenshot`、`check-update`、`EXEC` 后返回正确结果。

集成：

- 服务端启动后，Agent 主动连接并显示在线。
- 本机旧 agent 占用 `65432` 时，新 agent 不受影响。
- 截图、关机、远程命令通过 WebSocket 工作。
- 数据库迁移后应用启动无 MyBatis 映射错误。

## 未决事项

1. 是否合并现有 `pc_device` 中同一台机器的多网卡记录。
2. WOL 是否需要在第一版支持多 MAC 管理；如果需要，应新增 `pc_device_mac` 表。
3. `agentId` 持久化位置：建议客户端运行目录下的本地状态文件，避免用户改配置时误改身份。
4. 是否在后台页面展示在线状态和 `last_seen_time`；建议第一版展示。
