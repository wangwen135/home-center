package com.wwh.home.center.security;

import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.common.util.TimeFormatUtil;
import com.wwh.home.center.model.entity.SecurityLog;
import com.wwh.home.center.security.model.SecurityOperTypeEnum;
import com.wwh.home.center.service.SecurityLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 禁用IP
 *
 * @author wangwh
 * @date 2024/01/09
 */
@Slf4j
@Component
public class IpBanManager {

    private static final int MAX_LOGIN_FAILURE = 5;
    private static final int BAN_TIME_DURATION_MS = 60 * 1000; //1分钟
    private static final long BAN_TIME_MILLISECONDS = 5 * 60 * 1000; // 5分钟

    @Autowired
    private SecurityLogService securityLogService;

    private Map<String, BanInfo> bannedIps = new ConcurrentHashMap<>();

    public boolean isIpBanned(String ipAddress) {
        BanInfo banInfo = bannedIps.get(ipAddress);

        if (banInfo != null && banInfo.getFailureCount() >= MAX_LOGIN_FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public void handleLoginFailure(String ipAddress) {
        BanInfo banInfo = bannedIps.get(ipAddress);

        if (banInfo == null) {
            banInfo = new BanInfo(ipAddress);
            bannedIps.put(ipAddress, banInfo);
        }

        banInfo.incrementFailureCount();

        if (banInfo.getFailureCount() >= MAX_LOGIN_FAILURE) {
            banInfo.setBanEndTime(System.currentTimeMillis() + BAN_TIME_MILLISECONDS);
            log.info("禁止IP地址：{} 直到：{}", ipAddress, TimeFormatUtil.format(banInfo.getBanEndTime()));

            insertSecurityLog(banInfo);
        }
    }

    private void insertSecurityLog(BanInfo banInfo) {
        SecurityLog sLog = new SecurityLog();
        sLog.setIpAddress(banInfo.getIpAddress());
        sLog.setOperationType(SecurityOperTypeEnum.BAN_IP.name());

        sLog.setOperationResult(banInfo.getIpAddress());
        sLog.setDescription("由于连续登录失败，而触发禁用IP地址：" + banInfo.getIpAddress() + " 直到：" + TimeFormatUtil.format(banInfo.getBanEndTime()));

        //请求方法，地址 等信息
        sLog.setHttpMethod(RequestUtil.getRequestMethod());
        sLog.setRequestUrl(RequestUtil.getRequestURI());
        sLog.setUserAgent(RequestUtil.getBrowserInfo());
        sLog.setReferrerUrl(RequestUtil.getReferrer());
        securityLogService.saveSysLog(sLog);
    }

    // 每分钟执行1次
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredBans() {
        long currentTime = System.currentTimeMillis();

        bannedIps.keySet().forEach(ipAddress -> {
            bannedIps.compute(ipAddress, (key, banInfo) -> {
                if (banInfo != null && currentTime >= banInfo.getBanEndTime()) {
                    log.info("清理过期IP：{}", banInfo.getIpAddress());
                    return null; // 移除过期的封禁记录
                } else {
                    return banInfo; // 保持不变
                }
            });
        });
    }

    private static class BanInfo {
        private String ipAddress;
        private int failureCount;
        private long banEndTime;

        public BanInfo(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void incrementFailureCount() {
            failureCount++;
            banEndTime = System.currentTimeMillis() + BAN_TIME_DURATION_MS;
        }

        public long getBanEndTime() {
            return banEndTime;
        }

        public void setBanEndTime(long banEndTime) {
            this.banEndTime = banEndTime;
        }
    }
}
