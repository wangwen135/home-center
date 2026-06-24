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

    /**
     * 获取某系统已分配的用户 ID 列表
     */
    List<Integer> getAssignedUserIds(Integer sysId);

    /**
     * 全量覆盖：将指定系统分配给给定用户列表。
     * <p>同步刷新受影响用户的内存会话，使变更即时生效。
     */
    void assignUsersToSystem(Integer sysId, List<Integer> userIds);
}
