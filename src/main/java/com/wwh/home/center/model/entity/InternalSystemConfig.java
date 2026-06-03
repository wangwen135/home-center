package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 内部系统配置
 * </p>
 *
 * @author wwh
 * @since 2024-01-24
 */
@Getter
@Setter
@TableName("internal_system_config")
@ApiModel(value = "InternalSystemConfig对象", description = "内部系统配置")
public class InternalSystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("系统名称")
    private String sysName;

    @ApiModelProperty("系统域名")
    private String sysDomain;

    @ApiModelProperty("系统描述")
    private String sysDescription;

    @ApiModelProperty("系统状态")
    private String sysStatus;

    @ApiModelProperty("系统图标")
    private String icon;

    @ApiModelProperty("公网地址")
    private String internetUrl;

    @ApiModelProperty("开放公网地址")
    private String openInternetUrl;

    @ApiModelProperty("内网地址")
    private String internalUrl;

    @ApiModelProperty("配置信息")
    private String config;

    @ApiModelProperty("排序字段")
    private Integer sort;

    @ApiModelProperty("备注信息")
    private String remark;

    @ApiModelProperty("0 正常 1 已禁用")
    private Boolean disabled;

    @ApiModelProperty("0 正常 1 已删除")
    private Boolean deleted;

    @ApiModelProperty("创建人")
    private Integer createBy;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @ApiModelProperty("修改人")
    private Integer updateBy;

    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;


}
