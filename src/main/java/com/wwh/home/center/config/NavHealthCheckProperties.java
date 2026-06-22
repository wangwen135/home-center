package com.wwh.home.center.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 导航入口健康检查配置
 *
 * <p>健康检查由后台定时任务执行，结果缓存到数据库，前端只展示已有状态。</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "nav.health-check")
public class NavHealthCheckProperties {

    /**
     * 是否启用定时健康检查
     */
    private boolean enabled = true;

    /**
     * 定时检查间隔（毫秒），默认 30 分钟
     */
    private long intervalMs = 30 * 60 * 1000L;

    /**
     * 单个入口检查超时时间（毫秒），默认 5 秒
     */
    private int timeoutMs = 5000;

    /**
     * 服务启动后延迟首次检查的时间（毫秒），默认 60 秒
     */
    private long initialDelayMs = 60 * 1000L;
}
