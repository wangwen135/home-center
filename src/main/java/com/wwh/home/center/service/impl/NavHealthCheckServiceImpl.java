package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wwh.home.center.config.NavHealthCheckProperties;
import com.wwh.home.center.dao.mapper.NavLinkMapper;
import com.wwh.home.center.dao.mapper.PrivateNavLinkMapper;
import com.wwh.home.center.model.entity.NavLink;
import com.wwh.home.center.model.entity.PrivateNavLink;
import com.wwh.home.center.service.NavHealthCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 导航入口健康检查服务实现
 *
 * @author wangwh
 */
@Slf4j
@Service
public class NavHealthCheckServiceImpl implements NavHealthCheckService {

    /**
     * 说明/命令类入口类型，无 HTTP 目标，默认跳过健康检查
     */
    private static final Set<String> SKIP_ENTRY_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("note", "ssh_rdp")));

    private static final String STATUS_NORMAL = "normal";
    private static final String STATUS_ABNORMAL = "abnormal";
    private static final String STATUS_TIMEOUT = "timeout";
    private static final String STATUS_SKIPPED = "skipped";

    private static final String USER_AGENT = "HomeCenter-HealthCheck/1.0";

    @Autowired
    private NavLinkMapper navLinkMapper;

    @Autowired
    private PrivateNavLinkMapper privateNavLinkMapper;

    @Autowired
    private NavHealthCheckProperties properties;

    @Autowired
    @Qualifier("handleExecutor")
    private Executor handleExecutor;

    @Override
    public NavLink checkPublicLink(Long id) {
        NavLink link = navLinkMapper.selectById(id);
        if (link == null) {
            return null;
        }
        HealthResult result = checkUrl(link.getUrl(), null);
        persistPublic(id, result);
        return navLinkMapper.selectById(id);
    }

    @Override
    public int checkAllPublic() {
        List<NavLink> links = navLinkMapper.selectList(new LambdaQueryWrapper<NavLink>()
                .eq(NavLink::getStatus, 1));
        links.forEach(this::checkPublicAsync);
        return links.size();
    }

    @Override
    public int checkAllPrivate() {
        List<PrivateNavLink> links = privateNavLinkMapper.selectList(new LambdaQueryWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getStatus, 1));
        links.forEach(this::checkPrivateAsync);
        return links.size();
    }

    /**
     * 定时健康检查：启用时周期性检查全部公开与私有导航入口。
     * 单条检查超时较短，整体 sweep 提交到线程池并行执行，避免阻塞调度线程。
     */
    @Scheduled(fixedDelayString = "${nav.health-check.interval-ms:1800000}",
            initialDelayString = "${nav.health-check.initial-delay-ms:60000}")
    public void scheduledCheck() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            int total = checkAllPublic() + checkAllPrivate();
            log.info("导航健康检查任务已提交，待检查入口数：{}", total);
        } catch (Exception e) {
            log.error("导航健康检查任务异常", e);
        }
    }

    private void checkPublicAsync(NavLink link) {
        handleExecutor.execute(() -> {
            try {
                HealthResult result = checkUrl(link.getUrl(), null);
                persistPublic(link.getId(), result);
            } catch (Exception e) {
                log.warn("公开导航健康检查异常：id={}", link.getId(), e);
            }
        });
    }

    private void checkPrivateAsync(PrivateNavLink link) {
        handleExecutor.execute(() -> {
            try {
                HealthResult result = checkUrl(link.getUrl(), link.getEntryType());
                persistPrivate(link.getId(), result);
            } catch (Exception e) {
                log.warn("私有导航健康检查异常：id={}", link.getId(), e);
            }
        });
    }

    private void persistPublic(Long id, HealthResult result) {
        // 使用 LambdaUpdateWrapper 精准更新健康字段，不影响其他列
        navLinkMapper.update(null, new LambdaUpdateWrapper<NavLink>()
                .eq(NavLink::getId, id)
                .set(NavLink::getCheckStatus, result.status)
                .set(NavLink::getLastCheckTime, result.checkTime)
                .set(NavLink::getLastHttpStatus, result.httpStatus)
                .set(NavLink::getCheckDurationMs, result.durationMs)
                .set(NavLink::getLastFailReason, result.failReason));
    }

    private void persistPrivate(Long id, HealthResult result) {
        privateNavLinkMapper.update(null, new LambdaUpdateWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getId, id)
                .set(PrivateNavLink::getCheckStatus, result.status)
                .set(PrivateNavLink::getLastCheckTime, result.checkTime)
                .set(PrivateNavLink::getLastHttpStatus, result.httpStatus)
                .set(PrivateNavLink::getCheckDurationMs, result.durationMs)
                .set(PrivateNavLink::getLastFailReason, result.failReason));
    }

    /**
     * 根据入口类型和 URL 执行健康检查。
     *
     * <ul>
     *     <li>说明/命令类入口（note/ssh/rdp）或无 http(s) URL → skipped</li>
     *     <li>2xx/3xx → normal</li>
     *     <li>4xx/5xx → abnormal（记录 HTTP 状态码）</li>
     *     <li>超时 → timeout</li>
     *     <li>其他异常 → abnormal（记录原因）</li>
     * </ul>
     */
    private HealthResult checkUrl(String rawUrl, String entryType) {
        if (entryType != null && SKIP_ENTRY_TYPES.contains(entryType)) {
            return HealthResult.skipped();
        }
        String url = rawUrl == null ? null : rawUrl.trim();
        if (url == null || url.isEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) {
            return HealthResult.skipped();
        }
        long start = System.currentTimeMillis();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(properties.getTimeoutMs());
            conn.setReadTimeout(properties.getTimeoutMs());
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            int code = conn.getResponseCode();
            int duration = (int) (System.currentTimeMillis() - start);
            if (code >= 200 && code < 400) {
                return HealthResult.of(STATUS_NORMAL, code, duration, null);
            }
            return HealthResult.of(STATUS_ABNORMAL, code, duration, "HTTP " + code);
        } catch (SocketTimeoutException e) {
            int duration = (int) (System.currentTimeMillis() - start);
            return HealthResult.of(STATUS_TIMEOUT, null, duration, truncate("请求超时", 200));
        } catch (IOException e) {
            int duration = (int) (System.currentTimeMillis() - start);
            return HealthResult.of(STATUS_ABNORMAL, null, duration, truncate(e.getMessage(), 200));
        } catch (Exception e) {
            int duration = (int) (System.currentTimeMillis() - start);
            return HealthResult.of(STATUS_ABNORMAL, null, duration, truncate(e.getMessage(), 200));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    /**
     * 一次健康检查的结果
     */
    private static final class HealthResult {
        final String status;
        final Integer httpStatus;
        final int durationMs;
        final String failReason;
        final LocalDateTime checkTime = LocalDateTime.now();

        private HealthResult(String status, Integer httpStatus, int durationMs, String failReason) {
            this.status = status;
            this.httpStatus = httpStatus;
            this.durationMs = durationMs;
            this.failReason = failReason;
        }

        static HealthResult of(String status, Integer httpStatus, int durationMs, String failReason) {
            return new HealthResult(status, httpStatus, durationMs, failReason);
        }

        static HealthResult skipped() {
            return new HealthResult(STATUS_SKIPPED, null, 0, null);
        }
    }
}
