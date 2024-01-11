package com.wwh.home.center.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

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
     * 获取ID地址
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
}
