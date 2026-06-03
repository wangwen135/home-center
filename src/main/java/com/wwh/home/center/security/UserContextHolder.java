package com.wwh.home.center.security;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.model.entity.SysPermission;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
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

    private static final ThreadLocal<LocalHolderUser> userHolder = new ThreadLocal<>();

    protected static void setUserInfo(String token, LoggedUserAllInfo userInfo) {
        LocalHolderUser u = new LocalHolderUser(token, userInfo);
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
     * 获取用户信息
     *
     * @return 可能返回null
     */
    public static UserInfo getUserInfo() {
        LoggedUserAllInfo lui = getLoggedUserAllInfo();
        if (lui != null) {
            return lui.getUserInfo();
        }
        return null;
    }

    /**
     * 获取角色信息
     *
     * @return 可能返回null
     */
    public static SysRole getSysRole() {
        LoggedUserAllInfo lui = getLoggedUserAllInfo();
        if (lui != null) {
            return lui.getSysRole();
        }
        return null;
    }

    /**
     * 获取权限列表
     *
     * @return 可能返回null
     */
    public static List<SysPermission> getPermission() {
        LoggedUserAllInfo lui = getLoggedUserAllInfo();
        if (lui != null) {
            return lui.getPermissions();
        }
        return null;
    }

    /**
     * 是否是超级管理员
     *
     * @return
     */
    public static boolean isSuperAdmin() {
        SysRole role = getSysRole();
        if (role == null) {
            return false;
        }
        return role.getId() == SysConstants.SUPER_ADMIN_ROLE_ID;
    }


    /**
     * 获取登录用户信息
     *
     * @return 可能返回null
     */
    public static LoggedUserAllInfo getLoggedUserAllInfo() {
        LocalHolderUser lu = userHolder.get();
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
        LocalHolderUser lu = userHolder.get();
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

    private static class LocalHolderUser {
        private String token;
        private LoggedUserAllInfo loggedUserInfo;

        public LocalHolderUser(String token, LoggedUserAllInfo loggedUserInfo) {
            this.token = token;
            this.loggedUserInfo = loggedUserInfo;
        }

        public String getToken() {
            return token;
        }

        public LoggedUserAllInfo getLoggedUserInfo() {
            return loggedUserInfo;
        }
    }


}
