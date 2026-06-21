package com.wwh.home.center.security;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.util.PathMatchUtils;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.security.model.LoggedUserAllInfo;

import java.util.List;

public final class PermissionUtils {

    private PermissionUtils() {
    }

    public static boolean hasPermission(String path) {
        return hasPermission(path, UserContextHolder.getLoggedUserAllInfo());
    }

    public static boolean hasPermission(String path, LoggedUserAllInfo loggedUserInfo) {
        if (loggedUserInfo == null) {
            return false;
        }
        SysRole sysRole = loggedUserInfo.getSysRole();
        if (sysRole == null) {
            return false;
        }
        if (sysRole.getId() == SysConstants.SUPER_ADMIN_ROLE_ID) {
            return true;
        }

        List<String> urls = loggedUserInfo.getPlainUrls();
        if (urls != null && urls.contains(path)) {
            return true;
        }
        return PathMatchUtils.matchList(path, loggedUserInfo.getAntUrls());
    }
}
