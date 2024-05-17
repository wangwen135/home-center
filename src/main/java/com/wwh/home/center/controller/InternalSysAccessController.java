package com.wwh.home.center.controller;

import com.wwh.home.center.common.constant.SysConstants;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.wwh.home.center.security.TokenManager;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * 内部系统访问控制
 *
 * @author wangwh
 * @date 2024/05/17
 */
@Slf4j
@Validated
@Api(tags = "内部系统访问控制")
@RestController
public class InternalSysAccessController {

    //nginx内部调用的
    @ApiOperation("内部系统权限拦截")
    @RequestMapping(SysConstants.PATH_CHECK_AUTH) // /checkAuth
    public Result checkAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //请求的域名
        String domain = request.getServerName();
        log.trace("请求域名：{}", domain);

        //避免token键冲突，只从cookie中取值
        String token = RequestUtil.getTokenFromCookies(request);
        //获取登陆用户信息
        LoggedUserAllInfo userAllInfo = TokenManager.getUserAllInfoFromToken(token);
        if (userAllInfo == null) {
            log.trace("未登陆，或Token已过期，返回401");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return Result.unauthorized("未登陆，或Token已过期");
        }

        //判断用户是否有访问这个域名的权限
        String schemeDomain = "https://" + domain;
        List<InternalSystemConfig> systemList = userAllInfo.getUserSystem();
        for (InternalSystemConfig systemConfig : systemList) {
            if (systemConfig.getInternetUrl().startsWith(schemeDomain)) {
                return Result.success();
            }
        }
        log.debug("用户：{} 没有访问内部系统：{} 的权限", userAllInfo.getUserInfo().getUsername(), domain);

        //响应码为 403 Forbidden
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return Result.unauthorized("没有访问系统的权限");
    }

    @ApiOperation("预登陆接口")
    @RequestMapping("/preLogin")
    public void preLogin(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(required = false) String ref) throws IOException {
        if (StringUtils.isEmpty(ref)) {
            ref = request.getHeader("Referer");
        }
        log.debug("进入预登陆接口，来自：{}", ref);

        if (StringUtils.isEmpty(ref) || !ref.startsWith("http")) {
            log.debug("未知的来源，直接重定向到首页");
            response.sendRedirect("/");
            return;
        }

        String token = UserContextHolder.getToken();
        if (token == null) {
            log.debug("未登录，跳转到登录页面");
            response.sendRedirect("/login.html?ref=" + ref);
            return;
        }

        log.debug("已登录，注册token");
        String baseUrl = getBaseUrl(ref);
        String reqPart = ref.replaceFirst(baseUrl, "");
        reqPart = reqPart.equals("") ? "/" : reqPart;

        //注册地址固定
        response.sendRedirect(baseUrl + "/home-center/token?token=" + token + "&ref=" + reqPart);

    }


    private String getBaseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            if (port == -1) {
                return scheme + "://" + host;
            } else {
                return scheme + "://" + host + ":" + port;
            }
        } catch (URISyntaxException e) {
            log.error("URL语法错误", e);
            return null;
        }
    }
}
