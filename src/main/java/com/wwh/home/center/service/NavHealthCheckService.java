package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.NavLink;

/**
 * 导航入口健康检查服务
 *
 * <p>由后台定时任务执行，结果缓存到数据库；前端只展示已有状态，不在页面加载时探测。
 * 说明/命令类入口（无 URL 或 SSH/RDP/note 类型）默认跳过。</p>
 */
public interface NavHealthCheckService {

    /**
     * 手动检查单个公开导航入口，返回最新入口信息（含健康字段）。
     *
     * @param id 公开导航入口 ID
     * @return 更新后的入口；不存在返回 null
     */
    NavLink checkPublicLink(Long id);

    /**
     * 批量检查所有启用的公开导航入口，返回已检查的入口数量。
     *
     * @return 已检查入口数量
     */
    int checkAllPublic();

    /**
     * 批量检查所有启用的私有导航入口（全部用户），返回已检查的入口数量。
     *
     * @return 已检查入口数量
     */
    int checkAllPrivate();
}
