-- ============================================================================
-- 导航链接图标拆分
--   icon       -> 只存图片相对路径（如 2026/06/xxx.png）或外链地址；扩容到 VARCHAR(255)
--   icon_emoji -> 新增字段，存 emoji 或文字（与图片二选一，渲染时图片优先）
--
-- 注意：执行前请备份 nav_link 表。历史 icon 数据若为 emoji 会被迁移到 icon_emoji。
-- ============================================================================

-- 1. 新增 emoji 字段
ALTER TABLE `nav_link`
    ADD COLUMN `icon_emoji` VARCHAR(64) NULL COMMENT '图标(emoji或文字)' AFTER `icon`;

-- 2. icon 扩容到 255，兼容较长的相对路径 / 外链地址
ALTER TABLE `nav_link`
    MODIFY COLUMN `icon` VARCHAR(255) NULL COMMENT '图标图片相对路径或外链';

-- 3. 历史数据迁移：此前 icon 字段实际只用于 emoji，把"不像图片路径"的值搬到 icon_emoji
UPDATE `nav_link`
SET `icon_emoji` = `icon`
WHERE `icon` IS NOT NULL
  AND `icon` <> ''
  AND `icon` NOT LIKE 'http://%'
  AND `icon` NOT LIKE 'https://%'
  AND `icon` NOT REGEXP '[.](png|jpe?g|gif|webp|svg)$';

-- 4. 已迁移到 icon_emoji 的，从 icon 清空（避免被当成图片路径）
UPDATE `nav_link`
SET `icon` = NULL
WHERE `icon_emoji` IS NOT NULL
  AND `icon_emoji` <> '';
