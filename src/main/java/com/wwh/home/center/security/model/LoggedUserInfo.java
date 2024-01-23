package com.wwh.home.center.security.model;

import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录用户信息
 *
 * @author wangwh
 * @date 2024/01/09
 */
public class LoggedUserInfo {
    private UserInfo userInfo;
    private SysRole sysRole;
    private List<String> permissions;

    private List<String> plainUrls;
    private List<String> antUrls;

    private List<String> userSystem;

    public LoggedUserInfo(UserInfo userInfo, SysRole sysRole, List<String> permissions, List<String> userSystem) {
        this.userInfo = userInfo;
        this.sysRole = sysRole;
        this.permissions = permissions;
        this.userSystem = userSystem;
        processPermission(permissions);
    }

    private void processPermission(List<String> permission) {
        plainUrls = new ArrayList<>();
        antUrls = new ArrayList<>();

        if (permission == null) {
            return;
        }

        for (String p : permission) {
            if (StringUtils.isEmpty(p)) {
                continue;
            }
            //分号分隔的多个权限
            String[] parts = p.split(";");
            for (String part : parts) {
                if (part.contains("*") || part.contains("?")) {
                    antUrls.add(part);
                } else {
                    plainUrls.add(part);
                }
            }
        }
    }


    public UserInfo getUserInfo() {
        return userInfo;
    }


    public SysRole getSysRole() {
        return sysRole;
    }


    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getPlainUrls() {
        return plainUrls;
    }


    public List<String> getAntUrls() {
        return antUrls;
    }

    public List<String> getUserSystem() {
        return userSystem;
    }

}
