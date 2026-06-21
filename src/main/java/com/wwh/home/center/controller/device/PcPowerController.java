package com.wwh.home.center.controller.device;

import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import com.wwh.home.center.device.PcPowerEventProcessor;
import com.wwh.home.center.model.common.ApiResponse;
import com.wwh.home.center.model.entity.PcDevice;
import com.wwh.home.center.security.PermissionCodes;
import com.wwh.home.center.security.PermissionUtils;
import com.wwh.home.center.service.PcAgentAuditService;
import com.wwh.home.center.service.PcDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/device/pc")
public class PcPowerController {

    @Autowired
    private PcPowerEventProcessor pcPowerEventProcessor;

    @Autowired
    private PcDeviceService pcDeviceService;

    @Autowired
    private PcDeviceMapper pcDeviceMapper;

    @Autowired
    private PcAgentAuditService pcAgentAuditService;

    @GetMapping("/devices")
    public ApiResponse<List<PcDevice>> getAllDevices() {
        try {
            List<PcDevice> devices = pcDeviceService.getAllDevices();
            return ApiResponse.success("获取设备列表成功", devices);
        } catch (Exception e) {
            log.error("获取设备列表失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/permissions")
    public ApiResponse<Map<String, Boolean>> permissions() {
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("webShell", PermissionUtils.hasPermission(PermissionCodes.DEVICE_PC_WEB_SHELL));
        permissions.put("screenshot", PermissionUtils.hasPermission(PermissionCodes.DEVICE_PC_SCREENSHOT));
        permissions.put("power", PermissionUtils.hasPermission(PermissionCodes.DEVICE_PC_POWER));
        return ApiResponse.success("获取权限成功", permissions);
    }

    @PostMapping("/power/{deviceId}/{action}")
    public ApiResponse<Void> handlePowerAction(
            @PathVariable Long deviceId,
            @PathVariable String action) {
        log.info("收到电脑电源控制请求: deviceId={}, action={}", deviceId, action);

        String normalizedAction = action.toLowerCase();
        String requestId = pcAgentAuditService.newRequestId();
        LocalDateTime requestTime = LocalDateTime.now();
        PcDevice device = pcDeviceMapper.selectById(deviceId);
        String operationType = powerOperationType(normalizedAction);
        try {
            switch (normalizedAction) {
                case "on":
                    pcPowerEventProcessor.handlePowerOn(deviceId);
                    pcAgentAuditService.recordCommandOperation(requestId, device, operationType, null,
                            requestTime, LocalDateTime.now(), true, null, "power on command sent");
                    return ApiResponse.success("已发送开机指令");
                case "off":
                    pcPowerEventProcessor.handlePowerOff(deviceId);
                    pcAgentAuditService.recordCommandOperation(requestId, device, operationType, null,
                            requestTime, LocalDateTime.now(), true, null, "power off command sent");
                    return ApiResponse.success("已发送关机指令");
                case "restart":
                    pcPowerEventProcessor.handleRestart(deviceId);
                    pcAgentAuditService.recordCommandOperation(requestId, device, operationType, null,
                            requestTime, LocalDateTime.now(), true, null, "restart command sent");
                    return ApiResponse.success("已发送重启指令");
                default:
                    pcAgentAuditService.recordCommandOperation(requestId, device, operationType, null,
                            requestTime, LocalDateTime.now(), false, "不支持的操作", null);
                    return ApiResponse.error("不支持的操作");
            }
        } catch (Exception e) {
            log.error("处理电源控制请求失败", e);
            pcAgentAuditService.recordCommandOperation(requestId, device, operationType, null,
                    requestTime, LocalDateTime.now(), false, e.getMessage(), null);
            return ApiResponse.error(e.getMessage());
        }
    }

    private String powerOperationType(String action) {
        if ("off".equals(action)) {
            return "POWER_OFF";
        }
        if ("restart".equals(action)) {
            return "POWER_RESTART";
        }
        if ("on".equals(action)) {
            return "POWER_ON";
        }
        return "POWER_UNKNOWN";
    }
}
