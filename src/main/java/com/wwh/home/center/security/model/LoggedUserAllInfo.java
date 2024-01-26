package com.wwh.home.center.security.model;

import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.wwh.home.center.model.entity.SysPermission;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录用户的全信息
 *
 * @author wangwh
 * @date 2024/01/09
 */
public class LoggedUserAllInfo {
    private UserInfo userInfo;
    private SysRole sysRole;
    private List<SysPermission> permissions;

    private List<String> plainUrls;
    private List<String> antUrls;

    private List<InternalSystemConfig> userSystems;

    public LoggedUserAllInfo(UserInfo userInfo, SysRole sysRole, List<SysPermission> permissions,
                             List<InternalSystemConfig> userSystems) {
        Assert.notNull(userInfo, "用户信息不能为空");
        this.userInfo = userInfo;
        this.sysRole = sysRole;
        this.permissions = permissions;
        this.userSystems = userSystems;
        processPermission(permissions);
    }

    private void processPermission(List<SysPermission> permissions) {
        plainUrls = new ArrayList<>();
        antUrls = new ArrayList<>();

        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        for (SysPermission sp : permissions) {
            if (sp == null || StringUtils.isEmpty(sp.getUrls())) {
                continue;
            }
            //分号分隔的多个权限
            String[] parts = sp.getUrls().split(";");
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

    public List<SysPermission> getPermissions() {
        return permissions;
    }

    public List<String> getPlainUrls() {
        return plainUrls;
    }

    public List<String> getAntUrls() {
        return antUrls;
    }

    public List<InternalSystemConfig> getUserSystem() {
        return userSystems;
    }

    public String toSimpleString() {
        return "LoggedUserInfo{" +
                "userId=" + userInfo.getId() +
                ", userName=" + userInfo.getUsername() +
                ", roleId=" + sysRole.getId() +
                ", roleName=" + sysRole.getName() +
                '}';
    }
}
