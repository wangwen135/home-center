package com.wwh.home.center.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.wwh.home.center.common.constant.SysConstants.COOKIE_TOKEN_NAME;
import static com.wwh.home.center.common.constant.SysConstants.TOKEN_NAME;

/**
 * 请求工具
 *
 * @author wangwh
 * @date 2024/01/11
 */
@Slf4j
public class RequestUtil {

    /**
     * 获取请求对象
     *
     * @return
     */
    public static HttpServletRequest getRequestFromContextHolder() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return request;
    }

    /**
     * 获取请求URI
     *
     * @return
     */
    public static String getRequestURI() {
        HttpServletRequest request = getRequestFromContextHolder();
        if (request == null) {
            log.warn("无法获取HttpServletRequest...");
            return "";
        }
        return request.getRequestURI();
    }

    /**
     * 获得所有请求参数
     *
     * @param request
     * @return Map
     */
    public static Map<String, String[]> getParams(HttpServletRequest request) {
        if (request == null) {
            return new HashMap<>();
        }
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获得所有请求参数
     *
     * @param request
     * @return Map
     */
    public static Map<String, String> getParamMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return params;
    }


    /**
     * 获取IP地址
     *
     * @return
     */
    public static String getIpAddress() {
        HttpServletRequest request = getRequestFromContextHolder();
        if (request == null) {
            log.info("获取客户端IP地址时，无法获取HttpServletRequest...");
            return "unknown";
        }
        return getIpAddress(request);
    }

    /**
     * 获取IP地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        /**
         * <pre>
         * 直接在nginx想配置：
         *
         * #启用X-Real-IP头
         * real_ip_header X-Real-IP;
         * </pre>
         */
        // 优先获取X-Real-IP
        String clientIp = request.getHeader("X-Real-IP");

        //这个请求头是不可信的，可以被伪装
        // 如果X-Real-IP不存在，则获取X-Forwarded-For
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("X-Forwarded-For");
        }

        // 如果X-Forwarded-For不存在，则直接获取RemoteAddr
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        // 如果经过多级代理，X-Forwarded-For会有多个IP，第一个IP为真实客户端IP
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }

        return clientIp;
    }


    /**
     * 获取浏览器信息
     *
     * @return
     */
    public static String getBrowserInfo() {
        HttpServletRequest request = getRequestFromContextHolder();
        if (request == null) {
            log.info("获取客户端浏览器信息时，无法获取HttpServletRequest...");
            return "unknown";
        }
        return getBrowserInfo(request);
    }

    /**
     * 获取浏览器信息
     *
     * @param request
     * @return
     */
    public static String getBrowserInfo(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }


    /**
     * 从请求中获取token
     *
     * @param request
     * @return
     */
    public static String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_NAME);
        if (StringUtils.isNotEmpty(token)) {
            log.trace("从请求头中获取到 token = {}", token);
            return token;
        }
        token = request.getParameter(TOKEN_NAME);
        if (StringUtils.isNotEmpty(token)) {
            log.trace("从请求参数中获取到 token = {}", token);
            return token;
        }

        return getTokenFromCookies(request);
    }

    /**
     * 从cookies中获取token
     *
     * @param request
     * @return
     */
    public static String getTokenFromCookies(HttpServletRequest request) {
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
