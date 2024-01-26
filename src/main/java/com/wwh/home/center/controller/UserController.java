package com.wwh.home.center.controller;

import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.wwh.home.center.model.entity.SysPermission;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.vo.InternalSystemConfigVo;
import com.wwh.home.center.model.vo.SysPermissionVo;
import com.wwh.home.center.model.vo.SysRoleVo;
import com.wwh.home.center.model.vo.UserInfoVo;
import com.wwh.home.center.security.UserContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录用户相关
 *
 * @author wangwh
 * @date 2024/01/25
 */
@Slf4j
@Api(tags = "登录用户信息")
@Validated
@RestController
@RequestMapping("/user")
public class UserController {

    @ApiOperation("获取登录用户信息")
    @GetMapping("/info")
    public Result<UserInfoVo> getUserInfo() {
        UserContextHolder.isLoggedIn();
        UserInfo userInfo = UserContextHolder.getUserInfo();
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, vo);
        return Result.success(vo);
    }

    @ApiOperation("获取登录用户角色信息")
    @GetMapping("/role")
    public Result<SysRoleVo> getUserRole() {
        UserContextHolder.isLoggedIn();
        SysRole role = UserContextHolder.getSysRole();
        if (role == null) {
            Result.success();
        }
        SysRoleVo vo = new SysRoleVo();
        BeanUtils.copyProperties(role, vo);
        return Result.success(vo);
    }

    @ApiOperation("获取登录用户权限信息")
    @GetMapping("/permission")
    public Result<List<SysPermissionVo>> getUserPermission() {
        UserContextHolder.isLoggedIn();
        List<SysPermission> permissions = UserContextHolder.getPermission();

        if (permissions == null || permissions.isEmpty()) {
            return Result.success();
        }

        List<SysPermissionVo> resultList = new ArrayList<>();
        permissions.forEach(x -> {
            SysPermissionVo vo = new SysPermissionVo();
            BeanUtils.copyProperties(x, vo);
            resultList.add(vo);
        });
        return Result.success(resultList);
    }

    @ApiOperation("获取用户的内部系统信息")
    @GetMapping("/internalSystem")
    public Result getInternalSystem() {
        UserContextHolder.isLoggedIn();
        List<InternalSystemConfig> list = UserContextHolder.getLoggedUserAllInfo().getUserSystem();
        if (list == null || list.isEmpty()) {
            return Result.success();
        }
        List<InternalSystemConfigVo> resultList = new ArrayList<>();
        list.forEach(x -> {
            InternalSystemConfigVo vo = new InternalSystemConfigVo();
            BeanUtils.copyProperties(x, vo);
            resultList.add(vo);
        });
        return Result.success(resultList);
    }
}


