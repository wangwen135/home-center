package com.wwh.home.center.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限
 *
 * @author wangwh
 * @date 2024/01/26
 */

@ApiModel(description = "权限信息")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysPermissionVo {

    @ApiModelProperty("权限ID")
    private Integer id;

    @ApiModelProperty("父ID")
    private Integer pid;

    @ApiModelProperty("权限名")
    private String name;

    @ApiModelProperty("权限类型，1目录 2菜单 3按钮")
    private Integer type;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("排序")
    private Integer sort;

}
