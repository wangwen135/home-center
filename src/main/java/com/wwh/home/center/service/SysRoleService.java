package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.SysRole;

/**
 * 系统角色
 *
 * @author wangwh
 * @date 2024/01/09
 */
public interface SysRoleService {

    SysRole getRoleByUserId(Integer userId);

}
