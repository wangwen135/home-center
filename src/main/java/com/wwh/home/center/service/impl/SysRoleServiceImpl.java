package com.wwh.home.center.service.impl;

import com.wwh.home.center.dao.mapper.SysRoleMapper;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统角色服务
 *
 * @author wangwh
 * @date 2024/01/10
 */
@Slf4j
@Service
public class SysRoleServiceImpl implements SysRoleService {
    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public SysRole getRoleByUserId(Integer userId) {
        return null;
    }
}
