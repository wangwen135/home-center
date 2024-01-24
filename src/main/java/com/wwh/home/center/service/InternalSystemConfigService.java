package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.InternalSystemConfig;

import java.util.List;

/**
 * 内部系统配置服务
 *
 * @author wangwh
 * @date 2024/01/24
 */
public interface InternalSystemConfigService {

    /**
     * 获取用户的系统
     *
     * @param userId
     * @return
     */
    List<InternalSystemConfig> getInternalSystemByUserId(Integer userId);
}
