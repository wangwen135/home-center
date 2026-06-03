package com.wwh.home.center.service.impl;

import com.wwh.home.center.dao.mapper.SecurityLogMapper;
import com.wwh.home.center.model.entity.SecurityLog;
import com.wwh.home.center.service.SecurityLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 安全日志服务
 *
 * @author wangwh
 * @date 2024/09/30
 */
@Slf4j
@Service
public class SecurityLogServiceImpl implements SecurityLogService {
    @Autowired
    private SecurityLogMapper securityLogMapper;

    @Async
    @Override
    public void saveSysLog(SecurityLog securityLog) {
        //debugger
        System.out.println(Thread.currentThread().getName());

        securityLog.setId(null);
        securityLog.setOperationTime(LocalDateTime.now());
        securityLogMapper.insert(securityLog);
    }
}
