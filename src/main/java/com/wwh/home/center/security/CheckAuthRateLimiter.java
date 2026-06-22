package com.wwh.home.center.security;

import com.wwh.home.center.config.CheckAuthRateLimitProperties;
import com.wwh.home.center.model.vo.RateLimitStatusVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /checkAuth 轻量 IP 限流器（纯内存）。
 *
 * <p>按真实客户端 IP 在固定时间窗口内计数，超过阈值则封禁一段时间。
 * 仅用于拦截异常高频请求，阈值宽松，正常浏览不受影响。
 * 状态保存在内存，重启即清空；后台可查看与手动释放 IP。</p>
 *
 * @author wangwh
 */
@Slf4j
@Component
public class CheckAuthRateLimiter {

    @Autowired
    private CheckAuthRateLimitProperties properties;

    private final Map<String, IpStat> statMap = new ConcurrentHashMap<>();

    /**
     * 判断指定 IP 是否允许访问 /checkAuth。
     *
     * @param ip 客户端 IP
     * @return true 允许放行；false 命中限流（已被封禁或本次触发封禁）
     */
    public boolean allow(String ip) {
        if (!properties.isEnabled() || ip == null || ip.isEmpty()) {
            return true;
        }
        long now = System.currentTimeMillis();
        IpStat stat = statMap.computeIfAbsent(ip, k -> new IpStat(ip));
        synchronized (stat) {
            stat.totalCount++;
            stat.lastRequestMs = now;

            // 仍在封禁期内
            if (stat.blockedUntilMs > now) {
                stat.rejectedCount++;
                return false;
            }

            // 窗口过期则重置
            long windowMs = properties.getWindowSeconds() * 1000L;
            if (now - stat.windowStartMs >= windowMs) {
                stat.windowStartMs = now;
                stat.windowCount = 0;
            }
            stat.windowCount++;

            // 超过阈值 → 触发封禁
            if (stat.windowCount > properties.getMaxRequests()) {
                stat.blockedUntilMs = now + properties.getBlockSeconds() * 1000L;
                stat.rejectedCount++;
                log.warn("/checkAuth 限流触发：ip={}, windowCount={}, 封禁{}秒",
                        ip, stat.windowCount, properties.getBlockSeconds());
                return false;
            }
            return true;
        }
    }

    /**
     * 全部限流状态（后台查看用）。
     */
    public List<RateLimitStatusVo> listStatus() {
        List<RateLimitStatusVo> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        statMap.values().forEach(stat -> {
            synchronized (stat) {
                RateLimitStatusVo vo = new RateLimitStatusVo();
                vo.setIp(stat.ip);
                vo.setWindowCount(stat.windowCount);
                vo.setTotalCount(stat.totalCount);
                vo.setRejectedCount(stat.rejectedCount);
                vo.setLastRequestTime(toTime(stat.lastRequestMs));
                vo.setBlocked(stat.blockedUntilMs > now);
                vo.setBlockedUntilTime(toTime(stat.blockedUntilMs));
                list.add(vo);
            }
        });
        return list;
    }

    /**
     * 释放单个 IP（清空其计数与封禁）。
     */
    public boolean release(String ip) {
        return statMap.remove(ip) != null;
    }

    /**
     * 释放全部 IP。
     */
    public void releaseAll() {
        statMap.clear();
    }

    /**
     * 每 5 分钟清理一次长期无活动的条目，避免内存无限增长。
     */
    @Scheduled(fixedRate = 5 * 60 * 1000L)
    public void cleanStale() {
        long now = System.currentTimeMillis();
        long keepAliveMs = Math.max(properties.getWindowSeconds(),
                properties.getBlockSeconds()) * 1000L * 2 + 60 * 1000L;
        statMap.keySet().forEach(ip -> {
            statMap.compute(ip, (key, stat) -> {
                if (stat == null) {
                    return null;
                }
                // 超过保留期且已不在封禁期 → 移除
                if (now - stat.lastRequestMs > keepAliveMs && stat.blockedUntilMs <= now) {
                    return null;
                }
                return stat;
            });
        });
    }

    private static LocalDateTime toTime(long millis) {
        if (millis <= 0) {
            return null;
        }
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 单个 IP 的统计与封禁状态
     */
    private static final class IpStat {
        final String ip;
        long windowStartMs = System.currentTimeMillis();
        long windowCount = 0;
        long totalCount = 0;
        long rejectedCount = 0;
        long lastRequestMs = 0;
        long blockedUntilMs = 0;

        IpStat(String ip) {
            this.ip = ip;
        }
    }
}
