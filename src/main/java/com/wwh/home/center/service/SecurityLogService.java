package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.SecurityLog;

import java.util.List;

/**
 * 安全日志服务
 *
 * @author wangwh
 * @date 2024/09/30
 */
public interface SecurityLogService {

    void saveSysLog(SecurityLog securityLog);

    List<SecurityLog> listAll();
}
