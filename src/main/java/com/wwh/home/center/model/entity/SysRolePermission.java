package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 角色权限关联表
 * </p>
 *
 * @author wwh
 * @since 2024-01-08
 */
@Getter
@Setter
@TableName("sys_role_permission")
@ApiModel(value = "SysRolePermission对象", description = "角色权限关联表")
public class SysRolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("角色ID")
    private Integer roleId;

    @ApiModelProperty("权限ID")
    private Integer permissionId;


}
