package com.wwh.home.center.service.impl;

import com.wwh.home.center.dao.mapper.OperationLogMapper;
import com.wwh.home.center.model.entity.OperationLog;
import com.wwh.home.center.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 操作日志 服务实现类
 *
 * @author wangwh
 * @date 2024/03/14
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;


    @Override
    public void saveOperationLog(OperationLog log) {
        operationLogMapper.insert(log);
    }
}
