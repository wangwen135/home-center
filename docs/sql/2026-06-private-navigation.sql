ALTER TABLE nav_link
    ADD COLUMN open_type VARCHAR(32) NOT NULL DEFAULT 'blank' COMMENT '打开方式：blank 新窗口，self 当前窗口' AFTER icon;

CREATE TABLE IF NOT EXISTS private_nav_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(255) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    INDEX idx_private_nav_category_user_sort (user_id, sort_order, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私有导航分组';

CREATE TABLE IF NOT EXISTS private_nav_link (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    category_id BIGINT NULL,
    title VARCHAR(100) NOT NULL,
    url VARCHAR(1024) NULL,
    description VARCHAR(500) NULL,
    icon VARCHAR(255) NULL,
    icon_emoji VARCHAR(32) NULL,
    entry_type VARCHAR(32) NOT NULL DEFAULT 'external' COMMENT '入口类型：nginx_proxy/external/intranet/note/ssh/rdp',
    open_type VARCHAR(32) NOT NULL DEFAULT 'blank' COMMENT '打开方式：blank/self',
    instruction TEXT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    INDEX idx_private_nav_link_user_category_sort (user_id, category_id, sort_order, id),
    INDEX idx_private_nav_link_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私有导航入口';
