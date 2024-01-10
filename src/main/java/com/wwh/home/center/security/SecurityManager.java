package com.wwh.home.center.security;

import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 安全管理
 *
 * @author wangwh
 * @date 2024/01/09
 */
@Slf4j
@Component
public class SecurityManager {
    @Autowired
    private UserService userService;

    @Autowired
    private IpBanManager ipBanManager;

    @Autowired
    private UsernameBanManager usernameBanManager;

    public UserInfo login(String username, String password) {
        String ipAddr = UserContextHolder.getRemoteIpAddress();
        // 登录前检查
        preLogin(username, ipAddr);

        //匹配密码
        List<UserInfo> list = userService.getUserByNameOrPhone(username);
        UserInfo u = null;
        for (UserInfo userInfo : list) {
            if (matchPwd(password, userInfo)) {
                u = userInfo;
                break;
            }
        }

        if (u == null) {
            loginFailed(username, ipAddr);
            throw new UnauthorizedException("用户名或密码错误");
        }

        //检查用户状态
        if (u.getDisabled()) {
            log.info("用户被禁用：{}", u);
            throw new UnauthorizedException("用户被禁用，请联系管理员");
        }
        if (u.getLocked()) {
            log.info("用户被锁定：{}", u);
            throw new UnauthorizedException("用户被锁定，请联系管理员");
        }
        if (u.getExpired()) {
            log.info("账号已过期：{}", u);
            throw new UnauthorizedException("账号已过期，请联系管理员");
        }

        //获取用户的角色和权限

        //
        loginSuccess();

        return u;
    }

    public void loginSuccess() {
        //记录操作日志

    }

    public void loginFailed(String username, String ipAddr) {
        //记录失败的IP和用户名
        ipBanManager.handleLoginFailure(ipAddr);
        usernameBanManager.handleLoginFailure(username);
    }

    private void preLogin(String username, String ipAddr) {
        //检查IP地址
        if (ipBanManager.isIpBanned(ipAddr)) {
            throw new UnauthorizedException("本IP地址被禁止登录");
        }

        //检查用户名
        if (usernameBanManager.isUsernameBanned(username)) {
            throw new UnauthorizedException("本用户被禁止登录");
        }
    }


    public boolean matchPwd(String pwd, UserInfo userInfo) {
        String hexPwd = DigestUtils.md5Hex(pwd + userInfo.getSalt());
        return hexPwd.equals(userInfo.getPassword());
    }


}
