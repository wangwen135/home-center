package com.wwh.home.center.controller.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO
 *
 * @author wangwh
 * @date 2023/05/15
 */
//@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 在请求处理前进行权限验证和控制逻辑
        // 可以根据请求的URL、请求方法等信息进行相应的权限判断
        // 如果权限验证不通过，可以返回适当的响应或抛出异常

        // 示例：检查用户是否具有某个权限
        if (!hasPermission(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    private boolean hasPermission(HttpServletRequest request) {
        // 在这里编写具体的权限判断逻辑
        // 可以通过请求的URL、请求方法等信息进行权限判断
        // 返回true表示有权限，返回false表示没有权限

        // 示例：判断是否有访问/admin路径的权限
        String requestUrl = request.getRequestURI();
        if (requestUrl.startsWith("/admin") && !request.isUserInRole("ADMIN")) {
            return false;
        }

        return true;
    }
}
