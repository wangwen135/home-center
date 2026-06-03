package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.SysLog;

/**
 * 系统日志
 *
 * @author wangwh
 * @date 2024/01/10
 */
public interface SysLogService {
    void saveSysLog(SysLog log);
}
