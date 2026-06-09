package com.wwh.home.center.controller.device;

import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import com.wwh.home.center.device.tools.SimpleSocketSender;
import com.wwh.home.center.model.CmdResult;
import com.wwh.home.center.model.common.ApiResponse;
import com.wwh.home.center.model.entity.PcDevice;
import com.wwh.home.center.model.qo.PcCommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/device/pc")
public class PcCommandController {

    @Autowired
    private PcDeviceMapper pcDeviceMapper;

    @Autowired
    private SimpleSocketSender simpleSocketSender;

    @Value("${agent.command-timeout-seconds:30}")
    private int defaultCommandTimeoutSeconds;

    @PostMapping("/command/{deviceId}")
    public ApiResponse<CmdResult> executeCommand(
            @PathVariable Long deviceId,
            @RequestBody PcCommandRequest request) {
        if (request == null || StringUtils.isBlank(request.getCommand())) {
            return ApiResponse.error("命令不能为空");
        }

        try {
            PcDevice device = pcDeviceMapper.selectById(deviceId);
            if (device == null || !Integer.valueOf(1).equals(device.getStatus())) {
                return ApiResponse.error("设备不存在或已禁用");
            }

            int timeoutSeconds = request.getTimeoutSeconds() == null
                    ? defaultCommandTimeoutSeconds
                    : request.getTimeoutSeconds();
            CmdResult result = simpleSocketSender.executeCommand(device, request.getCommand(), timeoutSeconds);
            return ApiResponse.success("命令执行完成", result);
        } catch (Exception e) {
            log.error("执行PC远程命令失败: deviceId={}", deviceId, e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
