package com.wwh.home.center.security;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.common.util.PathMatchUtils;
import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static com.wwh.home.center.common.constant.SysConstants.*;

/**
 * 登录拦截器
 *
 * @author wangwh
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 白名单
     * <pre>
     * ?：匹配一个字符
     * *：匹配零个或多个字符，但不包括路径分隔符（/）
     * **：匹配零个或多个目录或文件
     * </pre>
     */
    private static final List<String> WHITE_LIST = Arrays.asList("/login", "/logout", "/preLogin", "/login.html",
            "/favicon.ico", "/error", "/test/**");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String method = request.getMethod();
        String path = request.getRequestURI();
        log.debug("preHandle 进入登录拦截器:[{}]{}", method, path);
/*
        System.out.println("=======================preHandle=========================");
        System.out.println(handler);
        System.out.println(handler.getClass());
        System.out.println("=======================preHandle=========================");
*/
        //特殊请求直接放行，加快响应速度
        if (SysConstants.PATH_CHECK_AUTH.equals(path)) {
            return true;
        }

        //options请求直接放行
        if (HttpMethod.OPTIONS.matches(method)) {
            log.debug("放行Options请求：{}", path);
            return true;
        }

        //从请求头，请求参数，cookie 中获取token
        String token = RequestUtil.getTokenFromRequest(request);
        LoggedUserAllInfo loggedUserAllInfo = TokenManager.getUserAllInfoFromToken(token);

        //设置上下文
        if (loggedUserAllInfo != null) {
            UserContextHolder.setUserInfo(token, loggedUserAllInfo);
        }

        if (PathMatchUtils.matchList(path, WHITE_LIST)) {
            log.trace("在白名单中，允许访问");
            return true;
        }

        //静态资源
        if (handler instanceof org.springframework.web.servlet.resource.ResourceHttpRequestHandler) {
            log.trace("访问静态资源：[{}]{}", method, path);
            //只有在登录之后才能访问静态资源
            if (loggedUserAllInfo == null) {
                log.trace("用户未登录，重定向到登录页");
                response.sendRedirect("/login.html");
                return false;
            }
            return true;
        }

        //放行内部转发
        if (handler instanceof org.springframework.web.servlet.mvc.ParameterizableViewController) {
            log.trace("放行内部转发：[{}]{}", method, path);
            return true;
        }

        // 只拦截Controller中的方法
        // if (handler instanceof org.springframework.web.method.HandlerMethod) {
        //     return true;
        // }
        if (token == null) {
            log.trace("token为空，重新登录");
            throw new UnauthorizedException("token为空，请先登录系统");
        }
        if (loggedUserAllInfo == null) {
            log.trace("用户为空，重新登录");
            throw new UnauthorizedException("token已过期，请重新登录系统");
        }

        //接口请求刷新token
        TokenManager.refreshToken(token);

        //判断接口权限
        if (!hasPermission(path, loggedUserAllInfo)) {
            log.warn("用户：[{}] {} 越权访问：{}", loggedUserAllInfo.getUserInfo().getId(), loggedUserAllInfo.getUserInfo().getUsername(), path);
            throw new ForbiddenException("您没有该接口的访问权限");
        }
        return true;
    }

    private boolean hasPermission(String path, LoggedUserAllInfo loggedUserInfo) {
        SysRole sysRole = loggedUserInfo.getSysRole();
        if (sysRole == null) {
            log.debug("用户{} 没有角色信息", loggedUserInfo.getUserInfo().getId());
            return false;
        }
        //超级管理员有全部权限
        if (sysRole.getId() == SUPER_ADMIN_ROLE_ID) {
            return true;
        }

        List<String> urls = loggedUserInfo.getPlainUrls();
        if (urls.contains(path)) {
            return true;
        }
        //通配符匹配
        return PathMatchUtils.matchList(path, loggedUserInfo.getAntUrls());
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.trace("postHandle 离开拦截器 URI：{}", request.getRequestURI());

        UserContextHolder.removeUserInfo();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextHolder.removeUserInfo();
/*
        System.out.println("++++++++++++++++++++++++afterCompletion+++++++++++++++++++++++++++++");
        System.out.println(handler);
        System.out.println(handler.getClass());
        System.out.println(ex);
        System.out.println("++++++++++++++++++++++++afterCompletion+++++++++++++++++++++++++++++");
*/
    }
}
