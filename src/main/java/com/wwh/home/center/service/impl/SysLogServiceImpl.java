package com.wwh.home.center.service.impl;

import com.wwh.home.center.dao.mapper.SysLogMapper;
import com.wwh.home.center.model.entity.SysLog;
import com.wwh.home.center.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 系统日志
 *
 * @author wangwh
 * @date 2024/01/10
 */
@Slf4j
@Service
public class SysLogServiceImpl implements SysLogService {

    @Autowired
    private SysLogMapper sysLogMapper;

    @Async
    @Override
    public void saveSysLog(SysLog sysLog) {
        log.debug("保存系统日志：{}", sysLog);
        sysLog.setId(null);
        sysLog.setSysTime(LocalDateTime.now());
        sysLogMapper.insert(sysLog);
    }
}
