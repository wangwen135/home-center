package com.wwh.home.center.service.impl;

import com.wwh.home.center.dao.mapper.InternalSystemConfigMapper;
import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.wwh.home.center.service.InternalSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 内部系统配置服务
 *
 * @author wangwh
 * @date 2024/01/24
 */
@Slf4j
@Service
public class InternalSystemConfigServiceImpl implements InternalSystemConfigService {

    @Autowired
    private InternalSystemConfigMapper internalSystemConfigMapper;

    @Override
    public List<InternalSystemConfig> getInternalSystemByUserId(Integer userId) {
        return null;
    }
}
