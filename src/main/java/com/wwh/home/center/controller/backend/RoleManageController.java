package com.wwh.home.center.controller.backend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.dao.mapper.UserRoleMapper;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserRole;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "角色管理接口")
@Validated
@RestController
@RequestMapping("/backend/role")
public class RoleManageController {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @ApiOperation("角色列表")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        checkSuperAdmin();
        return Result.success(sysRoleService.listAll());
    }

    @ApiOperation("新建角色")
    @PostMapping("/create")
    public Result<Void> create(@RequestBody SysRole role) {
        checkSuperAdmin();
        sysRoleService.createRole(role);
        return Result.success();
    }

    @ApiOperation("更新角色")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysRole role) {
        checkSuperAdmin();
        sysRoleService.updateRole(role);
        return Result.success();
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        checkSuperAdmin();
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", id);
        if (userRoleMapper.selectCount(queryWrapper) > 0) {
            return Result.badRequest("该角色已有用户关联，不能删除");
        }
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @ApiOperation("获取角色权限ID")
    @GetMapping("/{id}/permissions")
    public Result<List<Integer>> permissions(@PathVariable("id") Integer id) {
        checkSuperAdmin();
        return Result.success(sysRoleService.getPermissionIds(id));
    }

    @ApiOperation("分配角色权限")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable("id") Integer id, @RequestBody List<Integer> permissionIds) {
        checkSuperAdmin();
        sysRoleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能访问后台管理接口");
        }
    }
}
