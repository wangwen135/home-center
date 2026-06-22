package com.wwh.home.center.security;

import com.wwh.home.center.common.enums.SysLogTypeEnum;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.model.entity.*;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import com.wwh.home.center.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private IpBanManager ipBanManager;

    @Autowired
    private UsernameBanManager usernameBanManager;

    /**
     * 生产 HTTPS 部署时 cookie 设置 Secure，本地 HTTP 开发可不设置
     */
    @Value("${home-center.cookie.secure:false}")
    private boolean cookieSecure;

    /**
     * 跨子域共享登录态时设置 Domain=.<public-domain>，留空则仅当前域名生效
     */
    @Value("${home-center.cookie.domain:}")
    private String cookieDomain;

    /**
     * SameSite 策略，默认 Lax
     */
    @Value("${home-center.cookie.same-site:Lax}")
    private String cookieSameSite;

    /**
     * 登录
     *
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    public String login(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        String ipAddr = RequestUtil.getIpAddress(request);
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
            loginFailed(username, password, ipAddr, request);
            throw new UnauthorizedException("用户名或密码错误");
        }

        //检查用户状态
        if (user.getDisabled()) {
            log.info("用户被禁用：{}", user);
            loginSuccess(user, username, "用户被禁用");
            throw new UnauthorizedException("用户被禁用，请联系管理员");
        }
        if (user.getLocked()) {
            log.info("用户被锁定：{}", user);
            loginSuccess(user, username, "用户被锁定");
            throw new UnauthorizedException("用户被锁定，请联系管理员");
        }
        if (user.getExpired()) {
            log.info("账号已过期：{}", user);
            loginSuccess(user, username, "账号已过期");
            throw new UnauthorizedException("账号已过期，请联系管理员");
        }

        //日志
        loginSuccess(user, username, "");

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
        String originalUri = request.getParameter("ref");
        String token = TokenManager.generateToken(lui, ipAddr, RequestUtil.getBrowserInfo(request), originalUri);

        //写入cookie（手动构造 Set-Cookie 头以支持 SameSite/Secure/Domain）
        writeTokenCookie(response, token, -1);

        return token;
    }

    /**
     * 手动写入 home_center_token cookie，统一设置 HttpOnly、Path、SameSite、Secure、Domain。
     * Servlet 3.1 的 Cookie API 不支持 SameSite，因此通过 Set-Cookie 响应头控制。
     *
     * @param response 响应对象
     * @param token    token 值，为空表示清除 cookie
     * @param maxAge   有效期秒数，-1 为会话 cookie，0 为立即过期
     */
    private void writeTokenCookie(HttpServletResponse response, String token, int maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(COOKIE_TOKEN_NAME).append('=').append(token == null ? "" : token);
        sb.append("; Path=/");
        sb.append("; HttpOnly");
        if (StringUtils.isNotBlank(cookieSameSite)) {
            sb.append("; SameSite=").append(cookieSameSite);
        }
        if (cookieSecure) {
            sb.append("; Secure");
        }
        if (StringUtils.isNotBlank(cookieDomain)) {
            sb.append("; Domain=").append(cookieDomain);
        }
        if (maxAge >= 0) {
            sb.append("; Max-Age=").append(maxAge);
        }
        response.addHeader("Set-Cookie", sb.toString());
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

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //移除cookie（Max-Age=0 立即失效）
        writeTokenCookie(response, null, 0);

        //记录退出登录审计日志
        Integer userId = null;
        String username = null;
        try {
            userId = UserContextHolder.getUserId();
            username = UserContextHolder.getUsername();
        } catch (Exception ignored) {
            // 未登录场景忽略
        }
        if (userId != null) {
            SysLog sysLog = new SysLog();
            sysLog.setOperatorId(userId);
            sysLog.setOperatorName(username);
            sysLog.setLogType(SysLogTypeEnum.LOGOUT.toString());
            sysLog.setContent("用户主动退出登录");
            sysLog.setIp(RequestUtil.getIpAddress());
            sysLog.setBrowserInfo(RequestUtil.getBrowserInfo());
            sysLogService.saveSysLog(sysLog);
        }

        //移除token
        UserContextHolder.isLoggedIn();
        String token = UserContextHolder.getToken();
        TokenManager.removeToken(token);
    }

    private void loginSuccess(UserInfo user, String identity, String msg) {
        //记录操作日志
        String ipAddr = RequestUtil.getIpAddress();
        SysLog sysLog = new SysLog();
        sysLog.setOperatorId(user.getId());
        sysLog.setOperatorName(user.getUsername());
        sysLog.setLogType(SysLogTypeEnum.LOGIN.toString());
        sysLog.setContent("【" + identity + "】 + 【密码】 登录成功 " + msg);
        sysLog.setIp(ipAddr);
        sysLog.setBrowserInfo(RequestUtil.getBrowserInfo());
        sysLogService.saveSysLog(sysLog);
    }

    public void loginFailed(String username, String password, String ipAddr, HttpServletRequest request) {
        //记录失败的IP和用户名
        ipBanManager.handleLoginFailure(ipAddr);
        usernameBanManager.handleLoginFailure(username, password);

        //记录登录失败审计日志
        SysLog sysLog = new SysLog();
        sysLog.setOperatorName(username);
        sysLog.setLogType(SysLogTypeEnum.LOGIN.toString());
        sysLog.setContent("登录失败：用户名或密码错误");
        sysLog.setIp(ipAddr);
        sysLog.setBrowserInfo(RequestUtil.getBrowserInfo(request));
        sysLogService.saveSysLog(sysLog);
    }

    private void preLogin(String username, String ipAddr) {
        //检查IP地址
        if (ipBanManager.isIpBanned(ipAddr)) {
            throw new UnauthorizedException("IP地址【" + ipAddr + "】已被禁止登录");
        }

        //检查用户名
        if (usernameBanManager.isUsernameBanned(username)) {
            throw new UnauthorizedException("用户【" + username + "】已被禁止登录");
        }
    }


    private boolean matchPwd(String pwd, UserInfo userInfo) {
        // String hexPwd = DigestUtils.md5Hex(pwd + userInfo.getSalt());
        String hexPwd = DigestUtils.sha256Hex(pwd + userInfo.getSalt() + pwd);
        return hexPwd.equals(userInfo.getPassword());
    }


}
