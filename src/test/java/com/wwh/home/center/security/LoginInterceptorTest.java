package com.wwh.home.center.security;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.model.entity.SysPermission;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginInterceptorTest {

    private final LoginInterceptor interceptor = new LoginInterceptor();

    @AfterEach
    void 清理登录上下文() {
        UserContextHolder.removeUserInfo();
    }

    @Test
    void 白名单路径无需Token直接放行() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
    }

    @Test
    void Options请求直接放行() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/user/info");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
    }

    @Test
    void 静态资源未登录时直接放行() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/js/app.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new ResourceHttpRequestHandler());

        assertTrue(allowed);
        assertEquals(null, response.getRedirectedUrl());
    }

    @Test
    void 非白名单路径缺少Token时抛出未登录异常() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user/info");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(UnauthorizedException.class, () -> interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void 无效Token抛出过期异常() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user/info");
        request.addHeader(SysConstants.TOKEN_NAME, "not-exists");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(UnauthorizedException.class, () -> interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void 有效Token且有权限时放行并设置登录上下文() throws Exception {
        String token = TokenManager.generateToken(buildLoggedUser(2, "/user/info", null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user/info");
        request.addHeader(SysConstants.TOKEN_NAME, token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals("tester", UserContextHolder.getUsername());
        interceptor.afterCompletion(request, response, new Object(), null);
        assertEquals(null, UserContextHolder.getUsername());
        TokenManager.removeToken(token);
    }

    @Test
    void Token可以从Cookie读取并按通配符权限放行() throws Exception {
        String token = TokenManager.generateToken(buildLoggedUser(2, null, "/device/pc/**"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/device/pc/1/screenshot");
        request.setCookies(new Cookie(SysConstants.COOKIE_TOKEN_NAME, token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        TokenManager.removeToken(token);
    }

    @Test
    void 有Token但没有接口权限时抛出禁止访问异常() {
        String token = TokenManager.generateToken(buildLoggedUser(2, "/user/info", null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/device/pc/1");
        request.addHeader(SysConstants.TOKEN_NAME, token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(ForbiddenException.class, () -> interceptor.preHandle(request, response, new Object()));
        TokenManager.removeToken(token);
    }

    @Test
    void 超级管理员角色拥有全部接口权限() throws Exception {
        String token = TokenManager.generateToken(buildLoggedUser(SysConstants.SUPER_ADMIN_ROLE_ID, null, null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/any/path");
        request.addParameter(SysConstants.TOKEN_NAME, token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        TokenManager.removeToken(token);
    }

    private LoggedUserAllInfo buildLoggedUser(int roleId, String plainUrl, String antUrl) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(6);
        userInfo.setUsername("tester");

        SysRole role = new SysRole();
        role.setId(roleId);
        role.setName("role-" + roleId);

        if (plainUrl == null && antUrl == null) {
            return new LoggedUserAllInfo(userInfo, role, Collections.emptyList(), Collections.emptyList());
        }

        SysPermission permission = new SysPermission();
        permission.setUrls(joinUrls(plainUrl, antUrl));
        return new LoggedUserAllInfo(userInfo, role, Collections.singletonList(permission), Collections.emptyList());
    }

    private String joinUrls(String plainUrl, String antUrl) {
        return String.join(";", Arrays.asList(
                plainUrl == null ? "" : plainUrl,
                antUrl == null ? "" : antUrl
        ));
    }
}
