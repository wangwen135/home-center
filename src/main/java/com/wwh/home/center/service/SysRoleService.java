package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.SysRole;

import java.util.List;

/**
 * 系统角色
 *
 * @author wangwh
 * @date 2024/01/09
 */
public interface SysRoleService {

    SysRole getRoleByUserId(Integer userId);

    List<SysRole> listAll();

    void createRole(SysRole role);

    void updateRole(SysRole role);

    void deleteRole(Integer roleId);

    List<Integer> getPermissionIds(Integer roleId);

    void assignPermissions(Integer roleId, List<Integer> permissionIds);

}
