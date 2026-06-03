package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wwh.home.center.dao.mapper.SysPermissionMapper;
import com.wwh.home.center.model.entity.SysPermission;
import com.wwh.home.center.service.SysPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统权限
 *
 * @author wangwh
 * @date 2024/01/23
 */
@Slf4j
@Service
public class SysPermissionServiceImpl implements SysPermissionService {
    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysPermission> getPermissionByRoleId(Integer roleId) {
        return sysPermissionMapper.getPermissionByRoleId(roleId);
    }

    @Override
    public List<SysPermission> getAll() {
        QueryWrapper<SysPermission> queryWrapper = Wrappers.query();
        queryWrapper.eq("deleted", 0);
        queryWrapper.orderByAsc("pid", "sort");

        return sysPermissionMapper.selectList(queryWrapper);
    }
}
