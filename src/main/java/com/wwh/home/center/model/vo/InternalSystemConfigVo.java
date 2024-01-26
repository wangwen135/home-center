package com.wwh.home.center.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部系统配置
 *
 * @author wangwh
 * @date 2024/01/26
 */
@ApiModel(description = "内部系统配置信息")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InternalSystemConfigVo {

    @ApiModelProperty("ID")
    private Integer id;

    @ApiModelProperty("系统名称")
    private String sysName;

    @ApiModelProperty("系统标记")
    private String sysKey;

    @ApiModelProperty("系统描述")
    private String sysDescription;

    @ApiModelProperty("系统图标")
    private String icon;

    @ApiModelProperty("公网地址")
    private String internetUrl;

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

}
