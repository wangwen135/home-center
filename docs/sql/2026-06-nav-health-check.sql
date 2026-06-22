-- 导航入口健康检查字段
-- 服务端定时任务检查并缓存结果，前端只展示已有状态，不在页面加载时探测
-- 对公开导航(nav_link)和私有导航(private_nav_link)分别增加健康检查字段

-- 1. 公开导航：已有表，追加健康检查字段
ALTER TABLE nav_link
    ADD COLUMN check_status VARCHAR(16) NULL DEFAULT 'unknown'
        COMMENT '健康状态：unknown/normal/abnormal/timeout/skipped' AFTER status,
    ADD COLUMN last_check_time DATETIME NULL COMMENT '最近检查时间' AFTER check_status,
    ADD COLUMN last_http_status INT NULL COMMENT '最近检查 HTTP 状态码' AFTER last_check_time,
    ADD COLUMN check_duration_ms INT NULL COMMENT '最近检查耗时（毫秒）' AFTER last_http_status,
    ADD COLUMN last_fail_reason VARCHAR(255) NULL COMMENT '最近失败原因' AFTER check_duration_ms;

-- 2. 私有导航：完整建表（含健康检查字段），IF NOT EXISTS 保证可重复执行
CREATE TABLE IF NOT EXISTS private_nav_link (
    id bigint NOT NULL AUTO_INCREMENT,
    user_id int NOT NULL COMMENT '所属用户ID（按用户隔离）',
    category_id bigint DEFAULT NULL COMMENT '分组ID',
    title varchar(200) NOT NULL COMMENT '标题',
    url varchar(500) NOT NULL COMMENT '地址',
    description varchar(500) DEFAULT NULL COMMENT '描述',
    icon varchar(255) DEFAULT NULL COMMENT '图标图片相对路径或外链',
    icon_emoji varchar(64) DEFAULT NULL COMMENT '图标(emoji或文字)',
    entry_type varchar(32) DEFAULT 'link' COMMENT '入口类型：link/nginx_proxy/intranet/ssh_rdp/note',
    open_type varchar(32) DEFAULT '_blank' COMMENT '打开方式',
    instruction varchar(1000) DEFAULT NULL COMMENT '说明/命令类入口的指令文本',
    sort_order int DEFAULT 0 COMMENT '排序',
    status tinyint DEFAULT 1 COMMENT '1启用0禁用',
    check_status varchar(16) DEFAULT 'unknown' COMMENT '健康状态：unknown/normal/abnormal/timeout/skipped',
    last_check_time datetime DEFAULT NULL COMMENT '最近检查时间',
    last_http_status int DEFAULT NULL COMMENT '最近检查 HTTP 状态码',
    check_duration_ms int DEFAULT NULL COMMENT '最近检查耗时（毫秒）',
    last_fail_reason varchar(255) DEFAULT NULL COMMENT '最近失败原因',
    create_time datetime DEFAULT current_timestamp(),
    update_time datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='私有导航链接';

-- 3. 兼容：若 private_nav_link 已存在但缺字段，补列（忽略已存在列的错误）
ALTER TABLE private_nav_link
    ADD COLUMN IF NOT EXISTS (check_status VARCHAR(16) NULL DEFAULT 'unknown' COMMENT '健康状态：unknown/normal/abnormal/timeout/skipped'),
    ADD COLUMN IF NOT EXISTS (last_check_time DATETIME NULL COMMENT '最近检查时间'),
    ADD COLUMN IF NOT EXISTS (last_http_status INT NULL COMMENT '最近检查 HTTP 状态码'),
    ADD COLUMN IF NOT EXISTS (check_duration_ms INT NULL COMMENT '最近检查耗时（毫秒）'),
    ADD COLUMN IF NOT EXISTS (last_fail_reason VARCHAR(255) NULL COMMENT '最近失败原因');
