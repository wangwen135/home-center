package com.wwh.home.center.controller.backend;

import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.SysPermission;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.SysPermissionService;
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
@Api(tags = "权限管理接口")
@Validated
@RestController
@RequestMapping("/backend/permission")
public class PermissionManageController {

    @Autowired
    private SysPermissionService sysPermissionService;

    @ApiOperation("权限列表")
    @GetMapping("/list")
    public Result<List<SysPermission>> list() {
        checkSuperAdmin();
        return Result.success(sysPermissionService.getAll());
    }

    @ApiOperation("新建权限")
    @PostMapping("/create")
    public Result<Void> create(@RequestBody SysPermission permission) {
        checkSuperAdmin();
        sysPermissionService.createPermission(permission);
        return Result.success();
    }

    @ApiOperation("更新权限")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysPermission permission) {
        checkSuperAdmin();
        sysPermissionService.updatePermission(permission);
        return Result.success();
    }

    @ApiOperation("删除权限")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        checkSuperAdmin();
        sysPermissionService.deletePermission(id);
        return Result.success();
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能访问后台管理接口");
        }
    }
}
