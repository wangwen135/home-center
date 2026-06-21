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
