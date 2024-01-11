package com.wwh.home.center.security;

import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.security.model.LoggedUserInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <pre>
 * 线程局部变量保存登录用户信息
 * 注意：
 * 只在当前的请求线程中有效
 * 子线程、线程池请求手动传递用户信息
 * </pre>
 *
 * @author wangwh
 * @date 2024/01/10
 */
@Slf4j
public class UserContextHolder {

    private static final ThreadLocal<LocalUser> userHolder = new ThreadLocal<>();

    protected static void setUserInfo(String token, LoggedUserInfo userInfo) {
        LocalUser u = new LocalUser(token, userInfo);
        userHolder.set(u);
    }

    protected static void removeUserInfo() {
        userHolder.remove();
    }

    /**
     * 获取登录用户ID
     *
     * @return 可能返回null
     */
    public static Integer getUserId() {
        UserInfo userInfo = getUserInfo();
        if (userInfo != null) {
            return userInfo.getId();
        }
        return null;
    }

    /**
     * 获取登录用户名
     *
     * @return 可能返回null
     */
    public static String getUsername() {
        UserInfo userInfo = getUserInfo();
        if (userInfo != null) {
            return userInfo.getUsername();
        }
        return null;
    }

    /**
     * 获取登录用户信息
     *
     * @return 可能返回null
     */
    public static UserInfo getUserInfo() {
        LoggedUserInfo lui = getLoggedUserInfo();
        if (lui != null) {
            return lui.getUserInfo();
        }
        return null;
    }

    public static SysRole getSysRole() {
        LoggedUserInfo lui = getLoggedUserInfo();
        if (lui != null) {
            return lui.getSysRole();
        }
        return null;
    }

    public static List<String> getPermission() {
        LoggedUserInfo lui = getLoggedUserInfo();
        if (lui != null) {
            return lui.getPermission();
        }
        return null;
    }

    /**
     * 获取登录用户信息
     *
     * @return 可能返回null
     */
    public static LoggedUserInfo getLoggedUserInfo() {
        LocalUser lu = userHolder.get();
        if (lu != null) {
            return lu.getLoggedUserInfo();
        }
        return null;
    }

    /**
     * 获取当前Token
     *
     * @return 可能返回null
     */
    public static String getToken() {
        LocalUser lu = userHolder.get();
        if (lu != null) {
            return lu.getToken();
        }
        return null;
    }

    /**
     * 判断用户是否已登录<br>
     * 已登录返回用户id，否则抛出异常
     *
     * @return 返回用户ID
     */
    public static Integer isLoggedIn() {
        Integer userId = getUserId();
        if (userId == null) {
            throw new UnauthorizedException();
        }
        return userId;
    }

    private static class LocalUser {
        private String token;
        private LoggedUserInfo loggedUserInfo;

        public LocalUser(String token, LoggedUserInfo loggedUserInfo) {
            this.token = token;
            this.loggedUserInfo = loggedUserInfo;
        }

        public String getToken() {
            return token;
        }

        public LoggedUserInfo getLoggedUserInfo() {
            return loggedUserInfo;
        }
    }


}
