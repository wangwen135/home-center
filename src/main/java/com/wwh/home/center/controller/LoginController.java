package com.wwh.home.center.controller;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.wwh.home.center.security.LoginManager;
import com.wwh.home.center.security.TokenManager;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

import static com.wwh.home.center.common.constant.SysConstants.COOKIE_TOKEN_NAME;
import static com.wwh.home.center.common.constant.SysConstants.TOKEN_NAME;

/**
 * 登录
 *
 * @author wangwh
 * @date 2024/01/08
 */
@Slf4j
@Validated
@Api(tags = "登录")
@RestController
public class LoginController {

    @Autowired
    private LoginManager loginManager;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<String> login(@RequestParam @NotEmpty(message = "用户名不能为空") @ApiParam("用户名或手机号") String username
            , @RequestParam @NotEmpty(message = "密码不能为空") @ApiParam("密码") String password
            , HttpServletRequest request, HttpServletResponse response) {

        return Result.success(loginManager.login(username, password, response));
    }

    @ApiOperation("退出")
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            loginManager.logout(response);
        } catch (UnauthorizedException e) {
            log.debug("用户没有登录");
        }
        response.sendRedirect("/login.html");
        return Result.success();
    }

    @ApiOperation("内部系统权限拦截")
    @RequestMapping(SysConstants.PATH_CHECK_AUTH) // /checkAuth
    public Result checkAuth(HttpServletRequest request, HttpServletResponse response) {

        //避免token键冲突，只从cookie中取值
        String token = getTokenFromCookies(request);
        //获取登陆用户信息
        LoggedUserAllInfo userAllInfo = TokenManager.getUserAllInfoFromToken(token);

        if (userAllInfo == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return Result.unauthorized("未登陆，或Token已过期");
        }
        String sysKey = request.getParameter(SysConstants.INTERNAL_SYSTEM_KEY);
        if (StringUtils.isEmpty(sysKey)) {
            log.debug("内部系统权限拦截 缺少sys_key");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return Result.badRequest("缺少system key");
        }

        List<InternalSystemConfig> systemList = userAllInfo.getUserSystem();

        for (InternalSystemConfig systemConfig : systemList) {
            if (systemConfig.getSysKey().equals(sysKey)) {
                return Result.success();
            }
        }

        log.debug("用户：{} 没有访问内部系统：{} 的权限", userAllInfo.getUserInfo().getUsername(), sysKey);

        //响应码为 403 Forbidden
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return Result.unauthorized("没有访问系统的权限");
    }


    private String getTokenFromCookies(HttpServletRequest request) {
        // 获取所有的Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        // 遍历Cookie数组
        for (Cookie cookie : cookies) {
            // 判断是否是你之前设置的Cookie
            if (COOKIE_TOKEN_NAME.equals(cookie.getName())) {
                // 获取Cookie的值
                String cookieValue = cookie.getValue();
                log.trace("从Cookie中获取到 token = {}", cookieValue);
                return cookieValue;
            }
        }
        return null;
    }
}
