-- 用户待办事项（按 user_id 隔离，软删除，不物理删除）
-- 标签 tags 以英文逗号分隔内联存储（个人级数据，无需独立表）

CREATE TABLE IF NOT EXISTS todo_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '所属用户',
    content VARCHAR(500) NOT NULL COMMENT '内容',
    priority VARCHAR(10) NOT NULL DEFAULT 'low' COMMENT '优先级：high/medium/low',
    completed TINYINT NOT NULL DEFAULT 0 COMMENT '0 未完成 1 已完成',
    tags VARCHAR(500) NULL COMMENT '标签，英文逗号分隔',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0 正常 1 已删除（软删除，不物理删）',
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    INDEX idx_todo_user_active (user_id, deleted, completed, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户待办事项';

-- 权限点：待办事项 API（type=3 接口权限）
INSERT INTO sys_permission (pid, name, urls, type, icon, sort, deleted, create_by, create_time, update_time)
SELECT 0, 'Todo API', '/api/todo/**', 3, NULL, 240, 0, 1, NOW(), NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE urls = '/api/todo/**' AND deleted = 0);

-- 授权给所有未删除角色 ——「每个用户都有的权限」（超管本就放行全部，这里一并授予无副作用）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.urls = '/api/todo/**' AND p.deleted = 0
WHERE r.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_permission rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
