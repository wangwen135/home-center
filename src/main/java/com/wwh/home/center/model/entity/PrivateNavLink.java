package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("private_nav_link")
public class PrivateNavLink {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private Long categoryId;

    private String title;

    private String url;

    private String publicUrl;

    private String intranetUrl;

    private String description;

    private String icon;

    private String iconEmoji;

    private String entryType;

    private String openType;

    private String instruction;

    private Integer sortOrder;

    private Integer status;

    /**
     * 健康状态：unknown/normal/abnormal/timeout/skipped
     */
    private String checkStatus;

    /**
     * 最近检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastCheckTime;

    /**
     * 最近检查 HTTP 状态码
     */
    private Integer lastHttpStatus;

    /**
     * 最近检查耗时（毫秒）
     */
    private Integer checkDurationMs;

    /**
     * 最近失败原因
     */
    private String lastFailReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
