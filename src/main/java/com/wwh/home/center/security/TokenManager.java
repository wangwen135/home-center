package com.wwh.home.center.security;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.security.model.LoggedUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token
 *
 * @author wangwh
 * @date 2024/01/09
 */
@Slf4j
@Component
public class TokenManager {
    /**
     * token 过期时间，每次操作会自动续期
     */
    private static final long TOKEN_EXPIRATION_TIME_MS = 30 * 60 * 1000; // 30 分钟

    /**
     * token 最大存活时间，无论是否持续操作，到期自动失效
     */
    private static final long TOKEN_MAX_LIVE_TIME_MS = 12 * 60 * 60 * 1000;// 12小时

    private static final Map<String, TokenInfo> tokenMap = new ConcurrentHashMap<>();

    public static String generateToken(LoggedUserInfo userInfo) {
        Assert.notNull(userInfo, "用户信息不能为空");

        String token = UUID.randomUUID().toString().replace("-", "");
        long expirationTime = System.currentTimeMillis() + TOKEN_EXPIRATION_TIME_MS;
        TokenInfo tokenInfo = new TokenInfo(token, userInfo, expirationTime);
        tokenMap.put(token, tokenInfo);
        log.debug("生成新token：{}", token);
        return token;
    }

    public static void removeToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return;
        }
        tokenMap.remove(token);
        log.debug("移除token：{}", token);
    }

    public static void refreshToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return;
        }
        TokenInfo tokenInfo = tokenMap.get(token);
        if (tokenInfo != null) {
            tokenInfo.setExpirationTime(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME_MS);
        }
    }

    public static boolean isValidToken(String token) {
        TokenInfo tokenInfo = tokenMap.get(token);
        return tokenInfo != null && tokenInfo.getExpirationTime() > System.currentTimeMillis();
    }

    public static LoggedUserInfo getUserInfoFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        TokenInfo tokenInfo = tokenMap.get(token);
        return (tokenInfo != null && tokenInfo.getExpirationTime() > System.currentTimeMillis()) ? tokenInfo.getUserInfo() :
                null;
    }

    // 每分钟执行1次
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredToken() {
        long currentTime = System.currentTimeMillis();

        tokenMap.keySet().forEach(token -> {
            tokenMap.compute(token, (key, tokenInfo) -> {
                if (tokenInfo == null) {
                    return null;
                }
                if (currentTime >= tokenInfo.getExpirationTime()) {
                    log.debug("## 清理过期的Token：{}", tokenInfo);
                    return null; // 移除过期的Token
                } else if (currentTime >= tokenInfo.getCreateTime() + TOKEN_MAX_LIVE_TIME_MS) {
                    log.debug("## 清理超过最大存活时间的Token：{}", tokenInfo);
                    return null;
                } else {
                    return tokenInfo; // 保持不变
                }
            });
        });
    }

    private static class TokenInfo {
        private String token;
        private LoggedUserInfo userInfo;
        private long createTime;
        private long expirationTime;

        public TokenInfo(String token, LoggedUserInfo userInfo, long expirationTime) {
            this.token = token;
            this.userInfo = userInfo;
            this.expirationTime = expirationTime;
            this.createTime = System.currentTimeMillis();
        }

        public String getToken() {
            return token;
        }

        public LoggedUserInfo getUserInfo() {
            return userInfo;
        }

        public void setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public long getCreateTime() {
            return createTime;
        }

        @Override
        public String toString() {
            return "TokenInfo{" +
                    "token='" + token + '\'' +
                    ", userInfo=" + userInfo.toSimpleString() +
                    ", createTime=" + createTime +
                    ", expirationTime=" + expirationTime +
                    '}';
        }
    }
}
