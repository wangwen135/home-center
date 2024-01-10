package com.wwh.home.center.security;

import com.wwh.home.center.security.model.LoggedUserInfo;
import lombok.extern.slf4j.Slf4j;
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
    private static final long TOKEN_EXPIRATION_TIME_MS = 30 * 60 * 1000; // 30 分钟

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

    public static void refreshToken(String token) {
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
                if (tokenInfo != null && currentTime >= tokenInfo.getExpirationTime()) {
                    log.debug("清理过期的Token：{}", tokenInfo);
                    return null; // 移除过期的Token
                } else {
                    return tokenInfo; // 保持不变
                }
            });
        });
    }

    private static class TokenInfo {
        private String token;
        private LoggedUserInfo userInfo;
        private long expirationTime;

        public TokenInfo(String token, LoggedUserInfo userInfo, long expirationTime) {
            this.token = token;
            this.userInfo = userInfo;
            this.expirationTime = expirationTime;
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
    }
}
