package com.wwh.home.center.controller.backend;

import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.PcDevice;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.PcDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Api(tags = "PC设备后台管理接口")
@Validated
@RestController
@RequestMapping("/backend/device/pc")
public class PcDeviceManageController {

    @Autowired
    private PcDeviceService pcDeviceService;

    @ApiOperation("设备列表")
    @GetMapping("/list")
    public Result<List<PcDevice>> list() {
        checkSuperAdmin();
        return Result.success(pcDeviceService.getAllDevices());
    }

    @ApiOperation("添加设备")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody PcDevice device) {
        checkSuperAdmin();
        pcDeviceService.addDevice(device);
        return Result.success();
    }

    @ApiOperation("更新设备")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody PcDevice device) {
        checkSuperAdmin();
        pcDeviceService.updateDevice(device);
        return Result.success();
    }

    @ApiOperation("删除设备")
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam @NotNull(message = "设备ID不能为空") Long id) {
        checkSuperAdmin();
        pcDeviceService.deleteDevice(id);
        return Result.success();
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能访问后台管理接口");
        }
    }
}
