package com.wwh.home.center.security;

import com.wwh.home.center.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全拦截器
 *
 * @author wangwh
 */
@Slf4j
@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String method = request.getMethod();
        String path = request.getRequestURI();

        log.debug("preHandle 进入拦截器:[{}]{}", method, path);


        //options请求直接放行
        if (HttpMethod.OPTIONS.matches(method)) {
            log.debug("放行Options请求：{}", path);
            return true;
        }

        //静态资源直接放行
        if (handler instanceof org.springframework.web.servlet.resource.ResourceHttpRequestHandler) {
            log.debug("访问静态资源：[{}]{}", method, path);
            //登录校验？？
            return true;
        }

        // 只拦截Controller中的方法
//        if (handler instanceof org.springframework.web.method.HandlerMethod) {
//            return true;
//        }


        //从请求头，请求参数，cookie 中获取token
        String token = request.getParameter("token");
        if (StringUtils.isEmpty(token)) {
            token = request.getHeader("token");
        }
        log.debug("获取到 token = {}", token);

        if (StringUtils.isEmpty(token)) {
            throw new UnauthorizedException();
        }

        if (true) {
            return true;
        }
        // 重定向到登录
        response.sendRedirect("/admin/login");
        return false;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug("postHandle 离开拦截器 URI：{}", request.getRequestURI());

        UserContextHolder.removeUserInfo();
    }
}
