package com.wwh.home.center.controller.backend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.dao.mapper.SsoApplicationMapper;
import com.wwh.home.center.model.entity.SsoApplication;
import com.wwh.home.center.security.UserContextHolder;
import io.swagger.annotations.Api;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = "SSO Application Management")
@Validated
@RestController
@RequestMapping("/backend/sso/apps")
public class SsoApplicationManageController {

    @Autowired
    private SsoApplicationMapper ssoApplicationMapper;

    @GetMapping
    public Result<List<SsoApplication>> list() {
        checkSuperAdmin();
        List<SsoApplication> apps = ssoApplicationMapper.selectList(new LambdaQueryWrapper<SsoApplication>()
                .orderByAsc(SsoApplication::getAppId));
        apps.forEach(app -> app.setAppSecretHash(null));
        return Result.success(apps);
    }

    @PostMapping
    public Result<AppSecretResponse> create(@RequestBody @Valid SaveRequest request) {
        checkSuperAdmin();
        String appId = StringUtils.trimToEmpty(request.getAppId());
        ensureAppIdUnique(appId, null);

        String secret = generateSecret();
        LocalDateTime now = LocalDateTime.now();
        SsoApplication app = new SsoApplication();
        fill(app, request);
        app.setAppId(appId);
        app.setAppSecretHash(hashSecret(secret));
        app.setCreateTime(now);
        app.setUpdateTime(now);
        ssoApplicationMapper.insert(app);
        return Result.success(toSecretResponse(app, secret));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid SaveRequest request) {
        checkSuperAdmin();
        SsoApplication app = requireApp(id);
        String appId = StringUtils.trimToEmpty(request.getAppId());
        ensureAppIdUnique(appId, id);

        fill(app, request);
        app.setAppId(appId);
        app.setUpdateTime(LocalDateTime.now());
        ssoApplicationMapper.updateById(app);
        return Result.success();
    }

    @PostMapping("/{id}/reset-secret")
    public Result<AppSecretResponse> resetSecret(@PathVariable Long id) {
        checkSuperAdmin();
        SsoApplication app = requireApp(id);
        String secret = generateSecret();
        app.setAppSecretHash(hashSecret(secret));
        app.setUpdateTime(LocalDateTime.now());
        ssoApplicationMapper.updateById(app);
        return Result.success(toSecretResponse(app, secret));
    }

    @PutMapping("/{id}/status/{status}")
    public Result<Void> status(@PathVariable Long id, @PathVariable Integer status) {
        checkSuperAdmin();
        SsoApplication app = requireApp(id);
        app.setStatus(status != null && status == 1 ? 1 : 0);
        app.setUpdateTime(LocalDateTime.now());
        ssoApplicationMapper.updateById(app);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkSuperAdmin();
        ssoApplicationMapper.deleteById(id);
        return Result.success();
    }

    private void ensureAppIdUnique(String appId, Long excludeId) {
        LambdaQueryWrapper<SsoApplication> wrapper = new LambdaQueryWrapper<SsoApplication>()
                .eq(SsoApplication::getAppId, appId);
        if (excludeId != null) {
            wrapper.ne(SsoApplication::getId, excludeId);
        }
        Long exists = ssoApplicationMapper.selectCount(wrapper);
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("appId already exists");
        }
    }

    private void fill(SsoApplication app, SaveRequest request) {
        app.setAppName(StringUtils.trimToEmpty(request.getAppName()));
        app.setAllowedOrigins(StringUtils.trimToNull(request.getAllowedOrigins()));
        app.setAllowedScopes(StringUtils.defaultIfBlank(StringUtils.trimToNull(request.getAllowedScopes()), "basic"));
        app.setStatus(request.getStatus() == null || request.getStatus() == 1 ? 1 : 0);
        app.setRemark(StringUtils.trimToNull(request.getRemark()));
    }

    private SsoApplication requireApp(Long id) {
        SsoApplication app = ssoApplicationMapper.selectById(id);
        if (app == null) {
            throw new IllegalArgumentException("app not found");
        }
        return app;
    }

    private AppSecretResponse toSecretResponse(SsoApplication app, String secret) {
        AppSecretResponse response = new AppSecretResponse();
        response.setId(app.getId());
        response.setAppId(app.getAppId());
        response.setAppName(app.getAppName());
        response.setOneTimeSecret(secret);
        return response;
    }

    private String generateSecret() {
        return "hc_" + RandomStringUtils.randomAlphanumeric(40);
    }

    private String hashSecret(String secret) {
        return DigestUtils.sha256Hex(secret);
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("super admin required");
        }
    }

    @Data
    public static class SaveRequest {
        @NotBlank(message = "appId required")
        private String appId;

        @NotBlank(message = "appName required")
        private String appName;

        private String allowedOrigins;

        private String allowedScopes;

        private Integer status;

        private String remark;
    }

    @Data
    public static class AppSecretResponse {
        private Long id;
        private String appId;
        private String appName;
        private String oneTimeSecret;
    }
}
