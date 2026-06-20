package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pc_device")
public class PcDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String agentId;

    private String ipAddress;

    private String macAddress;

    private String hostname;

    private String osName;

    private String agentVersion;

    private Integer status;

    private LocalDateTime lastSeenTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
