package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.dao.mapper.SysPermissionMapper;
import com.wwh.home.center.model.entity.SysPermission;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.SysPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Override
    public void createPermission(SysPermission permission) {
        permission.setId(null);
        if (permission.getPid() == null) {
            permission.setPid(0);
        }
        if (permission.getSort() == null) {
            permission.setSort(0);
        }
        permission.setDeleted(false);
        permission.setCreateBy(UserContextHolder.getUserId());
        permission.setCreateTime(LocalDateTime.now());
        sysPermissionMapper.insert(permission);
    }

    @Override
    public void updatePermission(SysPermission permission) {
        SysPermission update = new SysPermission();
        update.setId(permission.getId());
        update.setPid(permission.getPid() == null ? 0 : permission.getPid());
        update.setName(permission.getName());
        update.setUrls(permission.getUrls());
        update.setType(permission.getType());
        update.setIcon(permission.getIcon());
        update.setSort(permission.getSort() == null ? 0 : permission.getSort());
        update.setUpdateBy(UserContextHolder.getUserId());
        update.setUpdateTime(LocalDateTime.now());
        sysPermissionMapper.updateById(update);
    }

    @Override
    public void deletePermission(Integer permissionId) {
        QueryWrapper<SysPermission> childWrapper = Wrappers.query();
        childWrapper.eq("pid", permissionId).eq("deleted", 0);
        if (sysPermissionMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException("请先删除子权限");
        }
        SysPermission update = new SysPermission();
        update.setId(permissionId);
        update.setDeleted(true);
        update.setUpdateBy(UserContextHolder.getUserId());
        update.setUpdateTime(LocalDateTime.now());
        sysPermissionMapper.updateById(update);
    }

    @Override
    public SysPermission getById(Integer id) {
        return sysPermissionMapper.selectById(id);
    }
}
