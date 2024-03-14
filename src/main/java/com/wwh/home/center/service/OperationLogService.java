package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.OperationLog;

/**
 * 操作日志 服务类
 *
 * @author wangwh
 * @date 2024/03/14
 */
public interface OperationLogService {

    void saveOperationLog(OperationLog log);
}
