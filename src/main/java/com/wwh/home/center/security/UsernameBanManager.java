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
 * 禁用账号
 *
 * @author wangwh
 * @date 2024/01/09
 */
@Slf4j
@Component
public class UsernameBanManager {

    private static final int MAX_LOGIN_FAILURE = 10;
    private static final int BAN_TIME_DURATION_MS = 60 * 1000; //1分钟
    private static final long BAN_TIME_MILLISECONDS = 5 * 60 * 1000; // 5分钟

    @Autowired
    private SecurityLogService securityLogService;

    private Map<String, BanInfo> bannedUsernames = new ConcurrentHashMap<>();

    public boolean isUsernameBanned(String username) {
        BanInfo banInfo = bannedUsernames.get(username);

        if (banInfo != null && banInfo.getFailureCount() >= MAX_LOGIN_FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public void handleLoginFailure(String username, String password) {
        BanInfo banInfo = bannedUsernames.get(username);

        if (banInfo == null) {
            banInfo = new BanInfo(username);
            bannedUsernames.put(username, banInfo);
        }

        banInfo.incrementFailureCount();

        if (banInfo.getFailureCount() >= MAX_LOGIN_FAILURE) {
            banInfo.setBanEndTime(System.currentTimeMillis() + BAN_TIME_MILLISECONDS);
            log.info("禁止用户：{} 直到：{}", username, TimeFormatUtil.format(banInfo.getBanEndTime()));

            insertSecurityLog(banInfo, password);
        }
    }


    private void insertSecurityLog(BanInfo banInfo, String password) {
        SecurityLog sLog = new SecurityLog();
        sLog.setIpAddress(RequestUtil.getIpAddress());
        sLog.setOperationType(SecurityOperTypeEnum.BAN_USERNAME.name());

        sLog.setOperationResult("");
        sLog.setDescription("由于连续登录失败，而触发禁用用户名：" + banInfo.getUsername() + " 直到：" + TimeFormatUtil.format(banInfo.getBanEndTime()));

        //请求方法，地址 等信息
        sLog.setHttpMethod(RequestUtil.getRequestMethod());
        sLog.setRequestUrl(RequestUtil.getRequestURI());
        sLog.setUserAgent(RequestUtil.getBrowserInfo());
        sLog.setReferrerUrl(RequestUtil.getReferrer());
        sLog.setRemark("尝试的密码为：" + password);
        securityLogService.saveSysLog(sLog);
    }

    // 每分钟执行1次
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredBans() {
        long currentTime = System.currentTimeMillis();

        bannedUsernames.keySet().forEach(ipAddress -> {
            bannedUsernames.compute(ipAddress, (key, banInfo) -> {
                if (banInfo != null && currentTime >= banInfo.getBanEndTime()) {
                    log.info("清理过期用户名：{}", banInfo.getUsername());
                    return null;
                } else {
                    return banInfo;
                }
            });
        });
    }

    private static class BanInfo {
        private String username;
        private int failureCount;
        private long banEndTime;

        public BanInfo(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
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
