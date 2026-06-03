package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.SysPermission;

import java.util.List;

/**
 * 系统权限
 *
 * @author wangwh
 * @date 2024/01/09
 */
public interface SysPermissionService {

    List<SysPermission> getPermissionByRoleId(Integer roleId);

    List<SysPermission> getAll();
}
