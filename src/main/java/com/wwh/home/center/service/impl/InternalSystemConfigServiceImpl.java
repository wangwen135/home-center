package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.dao.mapper.InternalSystemConfigMapper;
import com.wwh.home.center.dao.mapper.UserInternalSystemMapper;
import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.wwh.home.center.model.entity.UserInternalSystem;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.security.TokenManager;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.InternalSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private UserInternalSystemMapper userInternalSystemMapper;

    @Override
    public List<InternalSystemConfig> getInternalSystemByUserId(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空");
        return internalSystemConfigMapper.getInternalSystemByUserId(userId);
    }

    @Override
    public List<InternalSystemConfig> getAll() {
        QueryWrapper<InternalSystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        queryWrapper.orderByAsc("sort").orderByAsc("id");
        return internalSystemConfigMapper.selectList(queryWrapper);
    }

    @Override
    public List<InternalSystemConfig> getInternalSystemByLoginUser() {
        Integer userId = UserContextHolder.getUserId();

        List<InternalSystemConfig> list = getInternalSystemByUserId(userId);
        if (UserContextHolder.isSuperAdmin()) {
            return list;
        }
        list.forEach(x -> x.setRemark(null));
        return list;
    }

    @Override
    public List<Integer> getAssignedUserIds(Integer sysId) {
        Assert.notNull(sysId, "系统ID不能为空");
        QueryWrapper<UserInternalSystem> qw = new QueryWrapper<>();
        qw.eq("sys_id", sysId).eq("deleted", 0).select("user_id");
        return userInternalSystemMapper.selectList(qw).stream()
                .map(UserInternalSystem::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    public void assignUsersToSystem(Integer sysId, List<Integer> userIds) {
        Assert.notNull(sysId, "系统ID不能为空");
        if (userIds == null) {
            userIds = new ArrayList<>();
        }

        Integer operatorId = UserContextHolder.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // 当前已分配的用户列表（含已软删除的，用于判断是恢复还是新增）
        QueryWrapper<UserInternalSystem> existQw = new QueryWrapper<>();
        existQw.eq("sys_id", sysId);
        List<UserInternalSystem> existing = userInternalSystemMapper.selectList(existQw);

        Set<Integer> targetSet = new HashSet<>(userIds);

        // 所有受影响的用户（分配或取消分配）
        Set<Integer> affectedUserIds = new HashSet<>();

        for (UserInternalSystem rec : existing) {
            Integer uid = rec.getUserId();
            boolean isActive = rec.getDeleted() == null || !rec.getDeleted();

            if (targetSet.contains(uid)) {
                // 用户应被分配
                if (!isActive) {
                    // 恢复软删除记录
                    UpdateWrapper<UserInternalSystem> uw = new UpdateWrapper<>();
                    uw.eq("id", rec.getId())
                      .set("deleted", false)
                      .set("update_by", operatorId)
                      .set("update_time", now);
                    userInternalSystemMapper.update(null, uw);
                    affectedUserIds.add(uid);
                }
                // 已有效则无需操作
            } else {
                // 用户不应被分配
                if (isActive) {
                    // 软删除
                    UpdateWrapper<UserInternalSystem> uw = new UpdateWrapper<>();
                    uw.eq("id", rec.getId())
                      .set("deleted", true)
                      .set("update_by", operatorId)
                      .set("update_time", now);
                    userInternalSystemMapper.update(null, uw);
                    affectedUserIds.add(uid);
                }
            }
            // 标记为已处理，避免下方重复插入
            targetSet.remove(uid);
        }

        // targetSet 中剩余的是既没有有效记录也没有软删除记录的 —— 需要新增
        for (Integer uid : targetSet) {
            UserInternalSystem rec = new UserInternalSystem();
            rec.setSysId(sysId);
            rec.setUserId(uid);
            rec.setDeleted(false);
            rec.setCreateBy(operatorId);
            rec.setCreateTime(now);
            rec.setUpdateBy(operatorId);
            rec.setUpdateTime(now);
            userInternalSystemMapper.insert(rec);
            affectedUserIds.add(uid);
        }

        // 对所有受影响的非超管用户，刷新其内存会话中的系统列表
        for (Integer uid : affectedUserIds) {
            List<InternalSystemConfig> systems = getInternalSystemByUserId(uid);
            systems.forEach(s -> s.setRemark(null));
            TokenManager.refreshUserSystems(uid, systems);
        }
    }
}
