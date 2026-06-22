package com.wwh.home.center.security;

import com.wwh.home.center.common.enums.SysLogTypeEnum;
import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.model.entity.SysLog;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.vo.TokenVo;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import com.wwh.home.center.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Token / 登录会话管理
 *
 * <p>内存级单实例会话管理，token 为短期自定义 token，不使用 JWT、不依赖 Redis。
 * 同一用户只保留一个有效会话，新登录踢出旧会话；用户禁用、角色/权限变更、密码变更
 * 后立即踢出受影响会话；超管可踢出指定会话。所有失效都会记录审计日志。</p>
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

    private static final String STATUS_ONLINE = "online";

    private static final Map<String, TokenInfo> tokenMap = new ConcurrentHashMap<>();

    /**
     * 用于会话失效审计日志，通过实例 setter 注入到静态字段
     */
    private static SysLogService sysLogService;

    @Autowired
    private void initSysLogService(SysLogService sysLogService) {
        TokenManager.sysLogService = sysLogService;
    }

    /**
     * 获取全部在线会话信息（后台在线会话管理使用）
     *
     * @return 在线会话列表
     */
    public static List<TokenVo> getAll() {
        List<TokenVo> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        tokenMap.values().forEach(x -> {
            UserInfo userInfo = x.getUserAllInfo().getUserInfo();
            SysRole sysRole = x.getUserAllInfo().getSysRole();
            TokenVo vo = new TokenVo();
            vo.setToken("**********" + x.getToken().substring(10));
            vo.setCreateTime(millisToTime(x.getCreateTime()));
            vo.setExpirationTime(millisToTime(x.getExpirationTime()));
            vo.setMaxExpirationTime(millisToTime(x.getCreateTime() + TOKEN_MAX_LIVE_TIME_MS));
            vo.setUserId(userInfo == null ? null : userInfo.getId());
            vo.setUserName(userInfo == null ? null : userInfo.getUsername());
            vo.setRoleName(sysRole == null ? null : sysRole.getName());
            vo.setLoginIp(x.getLoginIp());
            vo.setLastAccessIp(x.getLastAccessIp());
            vo.setLastAccessTime(millisToTime(x.getLastAccessAt()));
            vo.setUserAgent(x.getUserAgent());
            vo.setOriginalUri(x.getOriginalUri());
            vo.setStatus(STATUS_ONLINE);
            // 计算剩余有效期，便于后台展示
            list.add(vo);
        });
        return list;
    }

    /**
     * 生成一个token（兼容旧调用，不记录登录上下文）
     */
    public static String generateToken(LoggedUserAllInfo userAllInfo) {
        return generateToken(userAllInfo, null, null, null);
    }

    /**
     * 生成一个token，并记录登录IP、User-Agent 和触发登录的原始目标地址。
     *
     * @param userAllInfo 登录用户信息
     * @param loginIp     登录IP
     * @param userAgent   登录 User-Agent
     * @param originalUri 触发登录的原始目标地址，可选
     * @return 新 token
     */
    public static String generateToken(LoggedUserAllInfo userAllInfo, String loginIp, String userAgent, String originalUri) {
        Assert.notNull(userAllInfo, "用户相关信息不能为空");
        UserInfo userInfo = userAllInfo.getUserInfo();
        Integer newUserId = userInfo == null ? null : userInfo.getId();
        if (newUserId != null) {
            // 同一用户只保留一个有效会话，新登录踢出旧会话并记录审计
            removeTokensByUserId(newUserId, "NEW_LOGIN");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        long now = System.currentTimeMillis();
        long expirationTime = now + TOKEN_EXPIRATION_TIME_MS;
        TokenInfo tokenInfo = new TokenInfo(token, userAllInfo, expirationTime, now);
        tokenInfo.setLoginIp(loginIp);
        tokenInfo.setLastAccessIp(loginIp);
        tokenInfo.setLastAccessAt(now);
        tokenInfo.setUserAgent(userAgent);
        tokenInfo.setOriginalUri(originalUri);
        tokenInfo.setStatus(STATUS_ONLINE);
        tokenMap.put(token, tokenInfo);
        log.debug("生成新token：{}", token);
        return token;
    }

    /**
     * 用户主动退出时移除 token。
     */
    public static void removeToken(String token) {
        removeToken(token, "LOGOUT", readCurrentActor());
    }

    /**
     * 移除指定 token 并记录失效原因与操作者。
     *
     * @param token           要移除的 token
     * @param reason          失效原因
     * @param invalidatedBy   失效操作者描述（例如超管用户名或 system）
     */
    public static void removeToken(String token, String reason, String invalidatedBy) {
        if (StringUtils.isEmpty(token)) {
            return;
        }
        TokenInfo info = tokenMap.remove(token);
        if (info != null) {
            log.info("移除会话：userId={}, reason={}, invalidatedBy={}",
                    info.getUserAllInfo().getUserInfo() == null ? null : info.getUserAllInfo().getUserInfo().getId(),
                    reason, invalidatedBy);
            recordInvalidation(info, reason, invalidatedBy);
        }
    }

    /**
     * 刷新token过期时间，并更新最近访问时间与访问IP。
     */
    public static void refreshToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return;
        }
        TokenInfo tokenInfo = tokenMap.get(token);
        if (tokenInfo != null) {
            long currentTime = System.currentTimeMillis();
            if (!isAlive(tokenInfo, currentTime)) {
                tokenMap.remove(token);
                return;
            }
            long maxExpireAt = tokenInfo.getCreateTime() + TOKEN_MAX_LIVE_TIME_MS;
            tokenInfo.setExpirationTime(Math.min(currentTime + TOKEN_EXPIRATION_TIME_MS, maxExpireAt));
            tokenInfo.setLastAccessAt(currentTime);
            String accessIp = safeRequestIp();
            if (accessIp != null) {
                tokenInfo.setLastAccessIp(accessIp);
            }
        }
    }

    /**
     * token是否有效
     */
    public static boolean isValidToken(String token) {
        TokenInfo tokenInfo = tokenMap.get(token);
        if (tokenInfo == null) {
            return false;
        }
        if (!isAlive(tokenInfo, System.currentTimeMillis())) {
            tokenMap.remove(token);
            return false;
        }
        return true;
    }

    /**
     * 根据token获取登录用户的全部信息
     */
    public static LoggedUserAllInfo getUserAllInfoFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        TokenInfo tokenInfo = tokenMap.get(token);
        if (tokenInfo == null) {
            return null;
        }
        if (!isAlive(tokenInfo, System.currentTimeMillis())) {
            tokenMap.remove(token);
            return null;
        }
        return tokenInfo.getUserAllInfo();
    }

    /**
     * 移除某用户的全部会话（系统触发：新登录、用户禁用、角色/权限变更、密码变更等）。
     * 统一记录会话失效审计日志。
     *
     * @param userId 用户ID
     * @param reason 失效原因
     */
    public static void removeTokensByUserId(Integer userId, String reason) {
        if (userId == null) {
            return;
        }
        String actor = readCurrentActor();
        tokenMap.forEach((token, tokenInfo) -> {
            UserInfo userInfo = tokenInfo.getUserAllInfo().getUserInfo();
            if (userInfo != null && userId.equals(userInfo.getId())) {
                tokenMap.remove(token);
                log.info("移除用户会话：userId={}, reason={}, invalidatedBy={}", userId, reason, actor);
                recordInvalidation(tokenInfo, reason, actor);
            }
        });
    }

    /**
     * 超管踢出某用户的当前会话。
     *
     * @param userId       被踢用户ID
     * @param operatorId   操作者ID（超管）
     * @param operatorName 操作者名称
     */
    public static void kickByUserId(Integer userId, Integer operatorId, String operatorName) {
        if (userId == null) {
            return;
        }
        String actor = StringUtils.isBlank(operatorName) ? ("user#" + operatorId) : operatorName;
        tokenMap.forEach((token, tokenInfo) -> {
            UserInfo userInfo = tokenInfo.getUserAllInfo().getUserInfo();
            if (userInfo != null && userId.equals(userInfo.getId())) {
                tokenMap.remove(token);
                log.info("超管踢出会话：userId={}, operator={}", userId, actor);
                recordInvalidation(tokenInfo, "ADMIN_KICK", actor);
            }
        });
    }

    /**
     * 判断某用户当前是否有在线会话。
     */
    public static boolean hasOnlineSession(Integer userId) {
        if (userId == null) {
            return false;
        }
        for (TokenInfo tokenInfo : tokenMap.values()) {
            UserInfo userInfo = tokenInfo.getUserAllInfo().getUserInfo();
            if (userInfo != null && userId.equals(userInfo.getId()) && isAlive(tokenInfo, System.currentTimeMillis())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 刷新某用户在所有有效 token 中的缓存信息。
     */
    public static void refreshUserInfo(Integer userId, Consumer<UserInfo> updater) {
        if (userId == null || updater == null) {
            return;
        }
        tokenMap.values().forEach(tokenInfo -> {
            UserInfo userInfo = tokenInfo.getUserAllInfo().getUserInfo();
            if (userInfo != null && userId.equals(userInfo.getId())) {
                updater.accept(userInfo);
            }
        });
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
                    recordInvalidation(tokenInfo, "EXPIRED", "system");
                    return null; // 移除过期的Token
                } else if (currentTime >= tokenInfo.getCreateTime() + TOKEN_MAX_LIVE_TIME_MS) {
                    log.debug("## 清理超过最大存活时间的Token：{}", tokenInfo);
                    recordInvalidation(tokenInfo, "MAX_LIVE_EXCEEDED", "system");
                    return null;
                } else {
                    return tokenInfo; // 保持不变
                }
            });
        });
    }

    private static boolean isAlive(TokenInfo tokenInfo, long currentTime) {
        return currentTime < tokenInfo.getExpirationTime()
                && currentTime < tokenInfo.getCreateTime() + TOKEN_MAX_LIVE_TIME_MS;
    }

    /**
     * 记录会话失效审计日志。actor 为触发者描述，用户身份信息从会话中取。
     */
    private static void recordInvalidation(TokenInfo info, String reason, String actor) {
        if (sysLogService == null) {
            return;
        }
        try {
            UserInfo userInfo = info.getUserAllInfo().getUserInfo();
            SysLog sysLog = new SysLog();
            sysLog.setOperatorId(userInfo == null ? null : userInfo.getId());
            sysLog.setOperatorName(userInfo == null ? null : userInfo.getUsername());
            sysLog.setLogType(SysLogTypeEnum.SESSION_INVALIDATED.toString());
            sysLog.setContent("会话失效：原因=" + reason + "，操作者=" + actor
                    + "，登录IP=" + info.getLoginIp() + "，最近IP=" + info.getLastAccessIp());
            sysLog.setIp(safeRequestIp());
            sysLog.setBrowserInfo(safeUserAgent());
            sysLogService.saveSysLog(sysLog);
        } catch (Exception e) {
            log.warn("记录会话失效审计日志异常：reason={}", reason, e);
        }
    }

    private static String readCurrentActor() {
        try {
            Integer id = UserContextHolder.getUserId();
            String name = UserContextHolder.getUsername();
            if (StringUtils.isNotBlank(name)) {
                return name + (id == null ? "" : ("#" + id));
            }
            return "system";
        } catch (Exception e) {
            return "system";
        }
    }

    private static String safeRequestIp() {
        try {
            return RequestUtil.getIpAddress();
        } catch (Exception e) {
            return null;
        }
    }

    private static String safeUserAgent() {
        try {
            return RequestUtil.getBrowserInfo();
        } catch (Exception e) {
            return null;
        }
    }

    private static java.time.LocalDateTime millisToTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static class TokenInfo {
        private String token;
        private LoggedUserAllInfo userAllInfo;
        private long createTime;
        private long expirationTime;
        private long lastAccessAt;
        private String loginIp;
        private String lastAccessIp;
        private String userAgent;
        private String originalUri;
        private String status;

        public TokenInfo(String token, LoggedUserAllInfo userAllInfo, long expirationTime, long createTime) {
            this.token = token;
            this.userAllInfo = userAllInfo;
            this.expirationTime = expirationTime;
            this.createTime = createTime;
        }

        public String getToken() {
            return token;
        }

        public LoggedUserAllInfo getUserAllInfo() {
            return userAllInfo;
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

        public long getLastAccessAt() {
            return lastAccessAt;
        }

        public void setLastAccessAt(long lastAccessAt) {
            this.lastAccessAt = lastAccessAt;
        }

        public String getLoginIp() {
            return loginIp;
        }

        public void setLoginIp(String loginIp) {
            this.loginIp = loginIp;
        }

        public String getLastAccessIp() {
            return lastAccessIp;
        }

        public void setLastAccessIp(String lastAccessIp) {
            this.lastAccessIp = lastAccessIp;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getOriginalUri() {
            return originalUri;
        }

        public void setOriginalUri(String originalUri) {
            this.originalUri = originalUri;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "TokenInfo{" +
                    "token='" + token + '\'' +
                    ", userInfo=" + userAllInfo.toSimpleString() +
                    ", createTime=" + createTime +
                    ", expirationTime=" + expirationTime +
                    '}';
        }
    }
}
