package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwh.home.center.dao.mapper.OperationLogMapper;
import com.wwh.home.center.model.entity.OperationLog;
import com.wwh.home.center.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public List<OperationLog> listAll() {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("oper_time").last("limit 200");
        return operationLogMapper.selectList(queryWrapper);
    }

    @Override
    public List<OperationLog> listPcAgentAudit(String operator, Long deviceId, String operationType, Integer status,
                                               LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("module", "PC_AGENT");
        if (StringUtils.isNotBlank(operationType)) {
            queryWrapper.eq("oper_type", operationType);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (startTime != null) {
            queryWrapper.ge("oper_time", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("oper_time", endTime);
        }
        if (StringUtils.isNotBlank(operator)) {
            queryWrapper.like("oper_param", "\"operatorUsername\":\"" + operator);
        }
        if (deviceId != null) {
            queryWrapper.like("oper_param", "\"deviceId\":" + deviceId);
        }
        queryWrapper.orderByDesc("oper_time").last("limit 500");
        return operationLogMapper.selectList(queryWrapper);
    }

    @Override
    public List<OperationLog> listSsoAudit(String appId, String user, Integer status,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("module", "SSO");
        if (StringUtils.isNotBlank(appId)) {
            queryWrapper.like("oper_param", "\"appId\":\"" + appId);
        }
        if (StringUtils.isNotBlank(user)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("oper_param", "\"username\":\"" + user)
                    .or()
                    .like("oper_param", "\"userId\":" + user));
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (startTime != null) {
            queryWrapper.ge("oper_time", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("oper_time", endTime);
        }
        queryWrapper.orderByDesc("oper_time").last("limit 500");
        return operationLogMapper.selectList(queryWrapper);
    }
}
