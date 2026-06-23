-- UI refactor follow-up migration.
-- Adds private navigation address-choice fields, SSO application management,
-- and fine-grained permission codes used by the split admin shell.

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE private_nav_link ADD COLUMN public_url VARCHAR(1024) NULL COMMENT ''Public access URL'' AFTER url',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'private_nav_link'
      AND COLUMN_NAME = 'public_url'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE private_nav_link ADD COLUMN intranet_url VARCHAR(1024) NULL COMMENT ''Intranet access URL'' AFTER public_url',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'private_nav_link'
      AND COLUMN_NAME = 'intranet_url'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS sso_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_id VARCHAR(100) NOT NULL,
    app_name VARCHAR(100) NOT NULL,
    app_secret_hash VARCHAR(128) NOT NULL,
    allowed_origins VARCHAR(1000) NULL,
    allowed_scopes VARCHAR(500) NULL DEFAULT 'basic',
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(500) NULL,
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    UNIQUE KEY uk_sso_application_app_id (app_id),
    INDEX idx_sso_application_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SSO application access configuration';

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'Admin Entry Menu', '/admin.html', 2, NULL, 80, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/admin.html' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'Private Navigation Admin Page', '/admin/private-nav.html', 2, NULL, 81, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/admin/private-nav.html' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'SSO Application Admin Page', '/admin/sso.html', 2, NULL, 93, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/admin/sso.html' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'Backend Public Navigation Management', '/backend/nav/**', 3, NULL, 151, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/nav/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'Backend Online Session Management', '/backend/token/**', 3, NULL, 152, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/token/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'Backend Rate Limit Management', '/backend/rate-limit/**', 3, NULL, 153, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/rate-limit/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'Backend Runtime Configuration', '/backend/config/**', 3, NULL, 154, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/config/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'Backend SSO Application Management', '/backend/sso/apps/**', 3, NULL, 155, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/sso/apps/**' AND deleted = 0);
