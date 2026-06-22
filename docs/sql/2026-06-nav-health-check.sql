-- 导航入口健康检查字段
-- 服务端定时任务检查并缓存结果，前端只展示已有状态，不在页面加载时探测
-- 对公开导航(nav_link)和私有导航(private_nav_link)分别增加健康检查字段

ALTER TABLE nav_link
    ADD COLUMN check_status VARCHAR(16) NULL DEFAULT 'unknown'
        COMMENT '健康状态：unknown/normal/abnormal/timeout/skipped' AFTER status,
    ADD COLUMN last_check_time DATETIME NULL COMMENT '最近检查时间' AFTER check_status,
    ADD COLUMN last_http_status INT NULL COMMENT '最近检查 HTTP 状态码' AFTER last_check_time,
    ADD COLUMN check_duration_ms INT NULL COMMENT '最近检查耗时（毫秒）' AFTER last_http_status,
    ADD COLUMN last_fail_reason VARCHAR(255) NULL COMMENT '最近失败原因' AFTER check_duration_ms;

ALTER TABLE private_nav_link
    ADD COLUMN check_status VARCHAR(16) NULL DEFAULT 'unknown'
        COMMENT '健康状态：unknown/normal/abnormal/timeout/skipped' AFTER status,
    ADD COLUMN last_check_time DATETIME NULL COMMENT '最近检查时间' AFTER check_status,
    ADD COLUMN last_http_status INT NULL COMMENT '最近检查 HTTP 状态码' AFTER last_check_time,
    ADD COLUMN check_duration_ms INT NULL COMMENT '最近检查耗时（毫秒）' AFTER last_http_status,
    ADD COLUMN last_fail_reason VARCHAR(255) NULL COMMENT '最近失败原因' AFTER check_duration_ms;
