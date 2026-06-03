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
     * 获取用户关联的系统
     *
     * @param userId
     * @return
     */
    List<InternalSystemConfig> getInternalSystemByUserId(Integer userId);

    /**
     * 获取全部
     *
     * @return
     */
    List<InternalSystemConfig> getAll();

    /**
     * <pre>
     * 根据登录用户关联的系统
     * 非超级管理员角色会清空备注信息
     * </pre>
     *
     * @return
     */
    List<InternalSystemConfig> getInternalSystemByLoginUser();
}
