package com.wwh.home.center.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.wwh.home.center.common.util.RequestUtil;
import com.wwh.home.center.model.CmdResult;
import com.wwh.home.center.model.entity.OperationLog;
import com.wwh.home.center.model.entity.PcDevice;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.OperationLogService;
import com.wwh.home.center.service.PcAgentAuditService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PcAgentAuditServiceImpl implements PcAgentAuditService {

    private static final String MODULE_PC_AGENT = "PC_AGENT";
    private static final int MAX_COMMAND_SUMMARY_LENGTH = 160;

    @Autowired
    private OperationLogService operationLogService;

    @Override
    public String newRequestId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void recordCommandOperation(String requestId, PcDevice device, String operationType, String commandSummary,
                                       LocalDateTime requestTime, LocalDateTime completeTime, boolean success,
                                       String failureReason, String resultSummary) {
        OperationLog log = new OperationLog();
        log.setUserId(UserContextHolder.getUserId());
        log.setOperTime(requestTime == null ? LocalDateTime.now() : requestTime);
        log.setOperType(operationType);
        log.setModule(MODULE_PC_AGENT);
        log.setRemark(device == null ? operationType : device.getName());
        log.setOperUrl(StringUtils.substring(RequestUtil.getRequestURI(), 0, 255));
        log.setMethod(RequestUtil.getRequestMethod());
        log.setStatus(success ? 0 : 1);
        log.setErrorMsg(success ? null : StringUtils.substring(failureReason, 0, 2000));
        log.setOperParam(StringUtils.substring(buildParam(requestId, device, operationType, commandSummary,
                requestTime, completeTime).toJSONString(), 0, 2000));
        log.setJsonResult(StringUtils.substring(buildResult(success, failureReason, resultSummary).toJSONString(), 0, 2000));
        operationLogService.saveOperationLog(log);
    }

    @Override
    public String summarizeCommand(String command) {
        String normalized = StringUtils.normalizeSpace(command);
        return StringUtils.substring(StringUtils.defaultIfBlank(normalized, "-"), 0, MAX_COMMAND_SUMMARY_LENGTH);
    }

    @Override
    public String summarizeCommandResult(CmdResult result) {
        if (result == null) {
            return "no result";
        }
        JSONObject summary = new JSONObject();
        summary.put("success", result.getSuccess());
        summary.put("exitCode", result.getExitCode());
        summary.put("timedOut", result.getTimedOut());
        summary.put("truncated", result.getTruncated());
        summary.put("durationMillis", result.getDurationMillis());
        summary.put("error", StringUtils.substring(result.getError(), 0, 300));
        return summary.toJSONString();
    }

    private JSONObject buildParam(String requestId, PcDevice device, String operationType, String commandSummary,
                                  LocalDateTime requestTime, LocalDateTime completeTime) {
        JSONObject param = new JSONObject();
        param.put("requestId", requestId);
        param.put("operatorUserId", UserContextHolder.getUserId());
        param.put("operatorUsername", UserContextHolder.getUsername());
        param.put("deviceId", device == null ? null : device.getId());
        param.put("agentId", device == null ? null : device.getAgentId());
        param.put("deviceName", device == null ? null : device.getName());
        param.put("operationType", operationType);
        param.put("commandSummary", commandSummary);
        param.put("requestTime", requestTime == null ? null : requestTime.toString());
        param.put("completeTime", completeTime == null ? null : completeTime.toString());
        param.put("durationMillis", durationMillis(requestTime, completeTime));
        param.put("sourceIp", RequestUtil.getIpAddress());
        param.put("userAgent", RequestUtil.getBrowserInfo());
        return param;
    }

    private JSONObject buildResult(boolean success, String failureReason, String resultSummary) {
        JSONObject result = new JSONObject();
        result.put("success", success);
        result.put("failureReason", failureReason);
        result.put("resultSummary", resultSummary);
        return result;
    }

    private Long durationMillis(LocalDateTime requestTime, LocalDateTime completeTime) {
        if (requestTime == null || completeTime == null) {
            return null;
        }
        return Duration.between(requestTime, completeTime).toMillis();
    }
}
