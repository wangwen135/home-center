package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导航链接
 */
@Data
@TableName("nav_link")
public class NavLink {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String title;

    private String url;

    private String description;

    private String icon;

    private Integer sortOrder;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
