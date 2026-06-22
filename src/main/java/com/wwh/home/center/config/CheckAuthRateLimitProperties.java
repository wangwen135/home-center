package com.wwh.home.center.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * /checkAuth 轻量限流配置
 *
 * <p>/checkAuth 由 nginx auth_request 在每个代理请求上调用，频率高。
 * 这里按真实客户端 IP 做轻量计数，仅在异常高频时拦截，避免拖垮应用。
 * 阈值故意设得宽松，不影响正常浏览。</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "check-auth.rate-limit")
public class CheckAuthRateLimitProperties {

    /**
     * 是否启用 /checkAuth 限流
     */
    private boolean enabled = true;

    /**
     * 单个时间窗口内允许的最大请求数（按 IP）
     */
    private int maxRequests = 600;

    /**
     * 统计窗口（秒）
     */
    private long windowSeconds = 60;

    /**
     * 超过阈值后封禁时长（秒）
     */
    private long blockSeconds = 60;
}
