# 自研应用 SSO 接入设计

## 目标

第一版 SSO 保持轻量，不引入 OAuth2 或 OIDC。自研应用与 Home Center 部署在同一父域的不同子域时，共享浏览器中的 `home_center_token` HttpOnly cookie，并通过 Home Center 接口确认登录态。

## 浏览器侧当前用户接口

接口：

```text
GET /api/sso/me
```

调用方式：

- 浏览器自动携带 `home_center_token` cookie。
- 推荐传入 `X-App-Id: <app-id>` 请求头；也兼容 `appId=<app-id>` 查询参数。
- 不要求浏览器传递 `appSecret`，也不允许把 `appSecret` 写入前端页面、静态 JS、浏览器请求或公开文档。

默认返回字段：

```json
{
  "userId": 1,
  "username": "user",
  "nickname": "nickname",
  "avatar": "/common/img/view/<avatar-path>",
  "roles": ["role-name"],
  "permissions": ["/api/example"]
}
```

默认信息范围为 `basic:userId,username,nickname,avatar,roles,permissions`。手机号、邮箱等敏感字段默认不返回。

## appId 与 appSecret

- `appId` 用于识别接入应用、审计调用来源和后续做应用级授权范围控制。
- `appSecret` 仅用于可信服务端到服务端调用，不进入浏览器。
- 后续后台应提供 appSecret 的生成、查看、重置和吊销能力。
- appSecret 应只展示一次或以脱敏形式展示，数据库保存时应使用不可逆摘要或加密存储方案。
- 服务端到服务端接口应同时校验 `appId`、`appSecret` 和应用启用状态。

## 应用授权范围

第一版只开放基础信息范围：

```text
basic:userId,username,nickname,avatar,roles,permissions
```

未来如某个自研应用确实需要手机号、邮箱等敏感字段，应在应用配置中显式授权额外 scope，并在 SSO 审计中记录返回的信息范围。

## 可信来源

第一版以 `appId` 作为审计和应用识别依据，不把 `appId` 当作安全边界。后续可信来源校验建议包括：

- 应用启用状态。
- 允许的回调地址或来源域名，统一使用 `<app>.<public-domain>` 形式的占位符。
- 服务端到服务端调用必须校验 appSecret。

## 登录回调与原始目标地址

自研应用发现未登录时，跳转到 Home Center 登录页，并通过 `ref` 传递原始目标地址：

```text
/login.html?ref=https://<app>.<public-domain>/<target-path>
```

登录成功后，Home Center 根据 `ref` 返回原始目标。nginx 代理系统继续沿用 `/preLogin`、`/checkAuth`、`/home-center/token` 流程。

## 统一退出与失效感知

Home Center 退出登录、token 过期、同用户新登录踢出旧会话、用户禁用、角色权限变更或密码变更后，服务端内存 token 失效。

自研应用不接收主动通知。它们在下一次调用 `/api/sso/me`、登录校验接口或自身后端鉴权时感知失效，并提示用户重新登录。

## Cookie 策略

生产环境默认全站 HTTPS，`home_center_token` cookie 策略应支持跨子域共享：

```text
Domain=.<public-domain>; Path=/; HttpOnly; Secure; SameSite=Lax
```

本地 HTTP 开发环境允许不设置 `Secure`。默认使用 `SameSite=Lax`；只有未来确实需要跨站 iframe 或第三方上下文时，才考虑 `SameSite=None; Secure`。

## 审计

`/api/sso/me` 每次调用都记录审计，字段包括：

- appId
- 用户 ID 和用户名
- endpoint
- callTime
- sourceIp
- User-Agent
- result
- failureReason
- returnedScope

后台 SSO 审计查询入口后续应只允许超管查看，并支持按 appId、用户、结果和时间范围筛选。
