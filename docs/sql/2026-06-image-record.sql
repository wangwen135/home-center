-- 图片上传记录表
-- 数据库只保存访问路径与必要元数据，不保存图片二进制
CREATE TABLE IF NOT EXISTS image_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    relative_path VARCHAR(255) NOT NULL COMMENT '相对访问路径，例如 2024/01/xxxx.png',
    original_name VARCHAR(255) NULL COMMENT '原始文件名',
    file_size BIGINT NULL COMMENT '文件大小（字节）',
    mime_type VARCHAR(64) NULL COMMENT 'MIME 类型',
    upload_time DATETIME NULL COMMENT '上传时间',
    upload_user_id INT NULL COMMENT '上传用户 ID',
    UNIQUE KEY uk_image_record_path (relative_path),
    INDEX idx_image_record_upload_time (upload_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片上传记录';
