package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.exception.BusinessException;
import com.wwh.home.center.dao.mapper.SysRoleMapper;
import com.wwh.home.center.dao.mapper.SysRolePermissionMapper;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.SysRolePermission;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public SysRole getRoleByUserId(Integer userId) {
        List<SysRole> list = sysRoleMapper.getRolesByUserId(userId);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<SysRole> listAll() {
        QueryWrapper<SysRole> queryWrapper = Wrappers.query();
        queryWrapper.eq("deleted", 0).orderByAsc("id");
        return sysRoleMapper.selectList(queryWrapper);
    }

    @Override
    public void createRole(SysRole role) {
        role.setId(null);
        role.setDeleted(false);
        role.setCreateBy(UserContextHolder.getUserId());
        role.setCreateTime(LocalDateTime.now());
        sysRoleMapper.insert(role);
    }

    @Override
    public void updateRole(SysRole role) {
        if (SysConstants.SUPER_ADMIN_ROLE_ID == role.getId()) {
            throw new BusinessException("超级管理员角色不可编辑");
        }
        SysRole update = new SysRole();
        update.setId(role.getId());
        update.setName(role.getName());
        update.setRemark(role.getRemark());
        update.setUpdateBy(UserContextHolder.getUserId());
        update.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(update);
    }

    @Override
    public void deleteRole(Integer roleId) {
        if (SysConstants.SUPER_ADMIN_ROLE_ID == roleId) {
            throw new BusinessException("超级管理员角色不可删除");
        }
        SysRole update = new SysRole();
        update.setId(roleId);
        update.setDeleted(true);
        update.setUpdateBy(UserContextHolder.getUserId());
        update.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(update);
    }

    @Override
    public List<Integer> getPermissionIds(Integer roleId) {
        QueryWrapper<SysRolePermission> queryWrapper = Wrappers.query();
        queryWrapper.eq("role_id", roleId);
        List<SysRolePermission> list = sysRolePermissionMapper.selectList(queryWrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Integer roleId, List<Integer> permissionIds) {
        QueryWrapper<SysRolePermission> deleteWrapper = Wrappers.query();
        deleteWrapper.eq("role_id", roleId);
        sysRolePermissionMapper.delete(deleteWrapper);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (Integer permissionId : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            sysRolePermissionMapper.insert(rolePermission);
        }
    }
}
