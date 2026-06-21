package com.wwh.home.center.service;

import com.wwh.home.center.model.CmdResult;
import com.wwh.home.center.model.entity.PcDevice;

import java.time.LocalDateTime;

public interface PcAgentAuditService {

    String newRequestId();

    void recordCommandOperation(String requestId, PcDevice device, String operationType, String commandSummary,
                                LocalDateTime requestTime, LocalDateTime completeTime, boolean success,
                                String failureReason, String resultSummary);

    String summarizeCommand(String command);

    String summarizeCommandResult(CmdResult result);
}
