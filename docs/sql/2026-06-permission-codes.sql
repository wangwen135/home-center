INSERT INTO sys_role (id, name, remark, deleted, create_time, update_time)
VALUES
    (1, 'super_admin', 'Super administrator with all permissions', 0, NOW(), NOW()),
    (2, 'normal_user', 'Normal user, permissions assigned explicitly', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    remark = VALUES(remark),
    deleted = 0,
    update_time = NOW();

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Public Home Menu', '/', 2, NULL, 10, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Private Navigation Menu', '/private.html', 2, NULL, 20, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/private.html' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Private Navigation API', '/api/private-nav/**', 3, NULL, 21, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/api/private-nav/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'PC Monitor Menu', '/device/pc/monitor.html', 2, NULL, 30, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/device/pc/monitor.html' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'PC Power Menu', '/device/pc/power.html', 2, NULL, 40, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/device/pc/power.html' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Backend Management Menu', '/admin/manage.html', 2, NULL, 90, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/admin/manage.html' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Online Session Management Menu', '/admin/sessions.html', 2, NULL, 91, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/admin/sessions.html' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Backend All', '/backend/**', 3, NULL, 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Backend User Management', '/backend/user/**', 3, NULL, 110, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/user/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Backend Role Management', '/backend/role/**', 3, NULL, 120, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/role/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Backend Permission Management', '/backend/permission/**', 3, NULL, 130, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/permission/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Backend PC Device Management', '/backend/device/pc/**', 3, NULL, 140, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/device/pc/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'Backend Data Management', '/backend/data/**', 3, NULL, 150, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/backend/data/**' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'PC Device View', '/device/pc/devices;/device/pc/permissions;/device/pc/*/screenshot/latest', 3, NULL, 200, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/device/pc/devices;/device/pc/permissions;/device/pc/*/screenshot/latest' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'PC Screenshot Capture', '/device/pc/*/screenshot', 3, NULL, 210, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/device/pc/*/screenshot' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'PC Power Control', '/device/pc/power/*/*', 3, NULL, 220, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/device/pc/power/*/*' AND deleted = 0);

INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_time, update_time)
SELECT 0, 'PC Web Shell', '/device/pc/command/*', 3, NULL, 230, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/device/pc/command/*' AND deleted = 0);
