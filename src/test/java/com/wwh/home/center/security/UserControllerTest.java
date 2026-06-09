package com.wwh.home.center.security;

import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.controller.UserController;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.vo.SysRoleVo;
import com.wwh.home.center.model.vo.UserInfoVo;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private final UserController userController = new UserController();

    @AfterEach
    void 清理登录上下文() {
        UserContextHolder.removeUserInfo();
    }

    @Test
    void 已登录用户可以获取登录信息并格式化头像() {
        UserInfo userInfo = buildUserInfo();
        UserContextHolder.setUserInfo("token-user", buildLoggedUser(userInfo, buildRole()));

        Result<UserInfoVo> result = userController.getUserInfo();

        assertEquals(200, result.getCode());
        assertEquals(7, result.getData().getId());
        assertEquals("tester", result.getData().getUsername());
        assertEquals("/common/img/view/avatar/test.png", result.getData().getAvatar());
    }

    @Test
    void 未登录获取用户信息抛出未授权异常() {
        assertThrows(UnauthorizedException.class, userController::getUserInfo);
    }

    @Test
    void 已登录用户可以获取角色信息() {
        UserContextHolder.setUserInfo("token-role", buildLoggedUser(buildUserInfo(), buildRole()));

        Result<SysRoleVo> result = userController.getUserRole();

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().getId());
        assertEquals("operator", result.getData().getName());
    }

    @Test
    void 已登录但没有角色时返回空数据() {
        UserContextHolder.setUserInfo("token-empty-role", buildLoggedUser(buildUserInfo(), null));

        Result<SysRoleVo> result = userController.getUserRole();

        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    private LoggedUserAllInfo buildLoggedUser(UserInfo userInfo, SysRole role) {
        return new LoggedUserAllInfo(userInfo, role, Collections.emptyList(), Collections.emptyList());
    }

    private UserInfo buildUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(7);
        userInfo.setUsername("tester");
        userInfo.setNickname("测试用户");
        userInfo.setAvatar("/avatar/test.png");
        return userInfo;
    }

    private SysRole buildRole() {
        SysRole role = new SysRole();
        role.setId(2);
        role.setName("operator");
        role.setRemark("操作员");
        return role;
    }
}
