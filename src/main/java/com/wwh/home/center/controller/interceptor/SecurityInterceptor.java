package com.wwh.home.center.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * TODO
 *
 * @author wangwh
 * @date 2023/04/27
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        logger.debug("进入拦截器 URI：{}", request.getRequestURI());

        HttpSession session = request.getSession();

        if (true) {
            return true;
        }
        // 重定向到登录
        response.sendRedirect("/admin/login");
        return false;

    }

}
