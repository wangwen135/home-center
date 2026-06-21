package com.wwh.home.center.controller.device;

import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import com.wwh.home.center.device.agent.AgentConnectionManager;
import com.wwh.home.center.model.CmdResult;
import com.wwh.home.center.model.common.ApiResponse;
import com.wwh.home.center.model.entity.PcDevice;
import com.wwh.home.center.model.qo.PcCommandRequest;
import com.wwh.home.center.service.PcAgentAuditService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/device/pc")
public class PcCommandController {

    @Autowired
    private PcDeviceMapper pcDeviceMapper;

    @Autowired
    private AgentConnectionManager agentConnectionManager;

    @Autowired
    private PcAgentAuditService pcAgentAuditService;

    @Value("${agent.command-timeout-seconds:30}")
    private int defaultCommandTimeoutSeconds;

    @PostMapping("/command/{deviceId}")
    public ApiResponse<CmdResult> executeCommand(
            @PathVariable Long deviceId,
            @RequestBody PcCommandRequest request) {
        if (request == null || StringUtils.isBlank(request.getCommand())) {
            return ApiResponse.error("命令不能为空");
        }

        String requestId = pcAgentAuditService.newRequestId();
        LocalDateTime requestTime = LocalDateTime.now();
        PcDevice device = null;
        String commandSummary = pcAgentAuditService.summarizeCommand(request.getCommand());
        try {
            device = pcDeviceMapper.selectById(deviceId);
            if (device == null || !Integer.valueOf(1).equals(device.getStatus())) {
                String message = "设备不存在或已禁用";
                pcAgentAuditService.recordCommandOperation(requestId, device, "REMOTE_COMMAND", commandSummary,
                        requestTime, LocalDateTime.now(), false, message, null);
                return ApiResponse.error(message);
            }

            int timeoutSeconds = request.getTimeoutSeconds() == null
                    ? defaultCommandTimeoutSeconds
                    : request.getTimeoutSeconds();
            CmdResult result = agentConnectionManager.executeCommand(device, request.getCommand(), timeoutSeconds);
            pcAgentAuditService.recordCommandOperation(requestId, device, "REMOTE_COMMAND", commandSummary,
                    requestTime, LocalDateTime.now(), true, null, pcAgentAuditService.summarizeCommandResult(result));
            return ApiResponse.success("命令执行完成", result);
        } catch (Exception e) {
            log.error("执行PC远程命令失败: deviceId={}", deviceId, e);
            pcAgentAuditService.recordCommandOperation(requestId, device, "REMOTE_COMMAND", commandSummary,
                    requestTime, LocalDateTime.now(), false, e.getMessage(), null);
            return ApiResponse.error(e.getMessage());
        }
    }
}
