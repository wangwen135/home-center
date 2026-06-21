package com.wwh.home.center.controller;

import com.alibaba.fastjson2.JSONObject;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.common.util.ImgUtils;
import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.model.entity.OperationLog;
import com.wwh.home.center.model.entity.SysPermission;
import com.wwh.home.center.model.entity.SysRole;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.vo.SsoCurrentUserVo;
import com.wwh.home.center.security.TokenManager;
import com.wwh.home.center.security.model.LoggedUserAllInfo;
import com.wwh.home.center.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Api(tags = "SSO接入接口")
@RestController
@RequestMapping("/api/sso")
public class SsoController {

    private static final String DEFAULT_APP_ID = "browser";
    private static final String SSO_SCOPE_BASIC = "basic:userId,username,nickname,avatar,roles,permissions";

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation("获取当前SSO登录用户")
    @GetMapping("/me")
    public Result<SsoCurrentUserVo> me(@RequestParam(required = false) String appId,
                                       @RequestHeader(value = "X-App-Id", required = false) String appIdHeader) {
        String resolvedAppId = resolveAppId(appId, appIdHeader);
        String token = RequestUtil.getTokenFromRequest(RequestUtil.getRequestFromContextHolder());
        LoggedUserAllInfo userAllInfo = TokenManager.getUserAllInfoFromToken(token);
        if (userAllInfo == null) {
            recordAudit(resolvedAppId, null, false, "token invalid or expired", SSO_SCOPE_BASIC);
            return Result.unauthorized("登录已失效，请重新登录");
        }

        TokenManager.refreshToken(token);
        SsoCurrentUserVo vo = buildCurrentUser(userAllInfo);
        recordAudit(resolvedAppId, userAllInfo.getUserInfo(), true, null, SSO_SCOPE_BASIC);
        return Result.success(vo);
    }

    private SsoCurrentUserVo buildCurrentUser(LoggedUserAllInfo userAllInfo) {
        UserInfo user = userAllInfo.getUserInfo();
        SsoCurrentUserVo vo = new SsoCurrentUserVo();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(ImgUtils.formatImagePath(user.getAvatar()));
        vo.setRoles(buildRoles(userAllInfo.getSysRole()));
        vo.setPermissions(buildPermissions(userAllInfo.getPermissions()));
        return vo;
    }

    private List<String> buildRoles(SysRole role) {
        if (role == null || StringUtils.isBlank(role.getName())) {
            return Collections.emptyList();
        }
        return Collections.singletonList(role.getName());
    }

    private List<String> buildPermissions(List<SysPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (SysPermission permission : permissions) {
            if (StringUtils.isNotBlank(permission.getUrls())) {
                String[] urls = permission.getUrls().split("[;,]");
                for (String url : urls) {
                    String value = StringUtils.trimToNull(url);
                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    private String resolveAppId(String appId, String appIdHeader) {
        return StringUtils.defaultIfBlank(StringUtils.trimToNull(appIdHeader),
                StringUtils.defaultIfBlank(StringUtils.trimToNull(appId), DEFAULT_APP_ID));
    }

    private void recordAudit(String appId, UserInfo user, boolean success, String failureReason, String returnedScope) {
        OperationLog log = new OperationLog();
        log.setUserId(user == null ? null : user.getId());
        log.setOperTime(LocalDateTime.now());
        log.setOperType("SSO_ME");
        log.setModule("SSO");
        log.setRemark(appId);
        log.setOperUrl(StringUtils.substring(RequestUtil.getRequestURI(), 0, 255));
        log.setMethod(RequestUtil.getRequestMethod());
        log.setStatus(success ? 0 : 1);
        log.setErrorMsg(success ? null : StringUtils.substring(failureReason, 0, 2000));
        log.setOperParam(StringUtils.substring(buildAuditParam(appId, user, returnedScope).toJSONString(), 0, 2000));
        log.setJsonResult(StringUtils.substring(buildAuditResult(success, failureReason, returnedScope).toJSONString(), 0, 2000));
        operationLogService.saveOperationLog(log);
    }

    private JSONObject buildAuditParam(String appId, UserInfo user, String returnedScope) {
        JSONObject param = new JSONObject();
        param.put("appId", appId);
        param.put("userId", user == null ? null : user.getId());
        param.put("username", user == null ? null : user.getUsername());
        param.put("endpoint", RequestUtil.getRequestURI());
        param.put("callTime", LocalDateTime.now().toString());
        param.put("sourceIp", RequestUtil.getIpAddress());
        param.put("userAgent", RequestUtil.getBrowserInfo());
        param.put("returnedScope", returnedScope);
        return param;
    }

    private JSONObject buildAuditResult(boolean success, String failureReason, String returnedScope) {
        JSONObject result = new JSONObject();
        result.put("success", success);
        result.put("failureReason", failureReason);
        result.put("returnedScope", returnedScope);
        return result;
    }
}
