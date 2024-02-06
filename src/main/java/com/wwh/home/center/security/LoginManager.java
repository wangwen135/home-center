package com.wwh.home.center.security;

import com.wwh.home.center.common.enums.SysLogTypeEnum;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.model.entity.*;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import com.wwh.home.center.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.wwh.home.center.common.constant.SysConstants.*;

/**
 * 登录管理器
 *
 * @author wangwh
 * @date 2024/01/09
 */
@Slf4j
@Component
public class LoginManager {
    @Autowired
    private UserService userService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private InternalSystemConfigService internalSystemConfigService;

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    public String login(String username, String password, HttpServletResponse response) {
        String ipAddr = RequestUtil.getIpAddress();
        // 登录前检查
        preLogin(username, ipAddr);

        //匹配密码
        List<UserInfo> list = userService.getUserByNameOrPhone(username);
        UserInfo user = null;
        for (UserInfo u : list) {
            if (matchPwd(password, u)) {
                user = u;
                break;
            }
        }

        if (user == null) {
            loginFailed(username, ipAddr);
            throw new UnauthorizedException("用户名或密码错误");
        }

        //检查用户状态
        if (user.getDisabled()) {
            log.info("用户被禁用：{}", user);
            throw new UnauthorizedException("用户被禁用，请联系管理员");
        }
        if (user.getLocked()) {
            log.info("用户被锁定：{}", user);
            throw new UnauthorizedException("用户被锁定，请联系管理员");
        }
        if (user.getExpired()) {
            log.info("账号已过期：{}", user);
            throw new UnauthorizedException("账号已过期，请联系管理员");
        }

        //日志
        loginSuccess(user, username);

        //清空用户密码
        user.setPassword(null);
        user.setSalt(null);

        //获取用户的角色
        SysRole sysRole = sysRoleService.getRoleByUserId(user.getId());
        if (sysRole == null) {
            throw new UnauthorizedException("该账号未配置角色，请联系管理员");
        }
        //获取权限
        List<SysPermission> permissionList = getSysPermissionByRole(sysRole);
        //获取内部系统权限
        List<InternalSystemConfig> userSystemList = getInternalSystemConfigs(user, sysRole);

        LoggedUserAllInfo lui = new LoggedUserAllInfo(user, sysRole, permissionList, userSystemList);
        String token = TokenManager.generateToken(lui);

        //写入cookie中
        Cookie cookie = new Cookie(COOKIE_TOKEN_NAME, token);
        //设置HttpOnly标志，无法通过脚本访问，降低XSS攻击风险
        cookie.setHttpOnly(true);
        // 设置Cookie的路径
        cookie.setPath("/");
        // 不设置Cookie的有效期，使其成为会话Cookie
        // cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);

        return token;
    }

    private List<SysPermission> getSysPermissionByRole(SysRole sysRole) {
        if (sysRole.getId() == SUPER_ADMIN_ROLE_ID) {
            //超级管理员有全部权限
            return sysPermissionService.getAll();
        }
        return sysPermissionService.getPermissionByRoleId(sysRole.getId());
    }

    private List<InternalSystemConfig> getInternalSystemConfigs(UserInfo user, SysRole sysRole) {
        if (sysRole.getId() == SUPER_ADMIN_ROLE_ID) {
            //超级管理员有全部系统
            return internalSystemConfigService.getAll();
        }
        List<InternalSystemConfig> userSystemList = internalSystemConfigService.getInternalSystemByUserId(user.getId());
        //不是超级管理员则需要清空备注信息
        userSystemList.forEach(x -> x.setRemark(null));
        return userSystemList;
    }

    public void logout(HttpServletResponse response) {
        //移除cookie
        // 创建一个同名的 Cookie，并将其有效期设置为 0，即立即过期
        Cookie cookie = new Cookie(COOKIE_TOKEN_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/"); // 设置Cookie的路径，确保与之前设置的路径一致
        cookie.setHttpOnly(true); // 设置为HttpOnly
        // 将新的 Cookie 添加到响应中
        response.addCookie(cookie);

        //移除token
        UserContextHolder.isLoggedIn();
        String token = UserContextHolder.getToken();
        TokenManager.removeToken(token);
    }

    private void loginSuccess(UserInfo user, String identity) {
        //记录操作日志
        String ipAddr = RequestUtil.getIpAddress();
        SysLog sysLog = new SysLog();
        sysLog.setOperatorId(user.getId());
        sysLog.setOperatorName(user.getUsername());
        sysLog.setLogType(SysLogTypeEnum.LOGIN.toString());
        sysLog.setContent("【" + identity + "】 + 【密码】 登录成功");
        sysLog.setIp(ipAddr);
        sysLog.setBrowserInfo(RequestUtil.getBrowserInfo());
        sysLogService.saveSysLog(sysLog);
    }

    public void loginFailed(String username, String ipAddr) {
        //记录失败的IP和用户名
        IpBanManager.handleLoginFailure(ipAddr);
        UsernameBanManager.handleLoginFailure(username);
    }

    private void preLogin(String username, String ipAddr) {
        //检查IP地址
        if (IpBanManager.isIpBanned(ipAddr)) {
            throw new UnauthorizedException("IP地址【" + ipAddr + "】已被禁止登录");
        }

        //检查用户名
        if (UsernameBanManager.isUsernameBanned(username)) {
            throw new UnauthorizedException("用户【" + username + "】已被禁止登录");
        }
    }


    private boolean matchPwd(String pwd, UserInfo userInfo) {
        String hexPwd = DigestUtils.md5Hex(pwd + userInfo.getSalt());
        return hexPwd.equals(userInfo.getPassword());
    }


}
