package com.wwh.home.center.controller.backend;

import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.SysConfig;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Slf4j
@Api(tags = "系统配置管理接口")
@Validated
@RestController
@RequestMapping("/backend/config")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @ApiOperation("获取所有配置")
    @GetMapping("/list")
    public Result<List<SysConfig>> list() {
        checkSuperAdmin();
        return Result.success(sysConfigService.listAll());
    }

    @ApiOperation("按key获取配置")
    @GetMapping("/getByKey")
    public Result<SysConfig> getByKey(@RequestParam("key") @NotEmpty(message = "配置key不能为空") String key) {
        checkSuperAdmin();
        return Result.success(sysConfigService.getByKey(key));
    }

    @ApiOperation("更新配置值")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid UpdateConfigRequest request) {
        checkSuperAdmin();
        log.info("后台更新系统配置，key={}", request.getKey());
        sysConfigService.updateConfig(request.getKey(), request.getValue());
        return Result.success();
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能访问后台管理接口");
        }
    }

    @Data
    public static class UpdateConfigRequest {

        @NotEmpty(message = "配置key不能为空")
        private String key;

        private String value;
    }
}
