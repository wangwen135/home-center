package com.wwh.home.center.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色信息
 *
 * @author wangwh
 * @date 2024/01/26
 */
@ApiModel(description = "角色信息")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysRoleVo {

    @ApiModelProperty("角色ID")
    private Integer id;

    @ApiModelProperty("角色名")
    private String name;

    @ApiModelProperty("备注信息")
    private String remark;

}
