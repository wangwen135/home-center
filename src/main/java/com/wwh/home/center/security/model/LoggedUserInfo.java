package com.wwh.home.center.security.model;

import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import lombok.Data;

import java.util.List;

/**
 * 登录用户信息
 *
 * @author wangwh
 * @date 2024/01/09
 */
@Data
public class LoggedUserInfo {
    private UserInfo userInfo;
    private SysRole sysRole;
    private List<String> permission;
}
