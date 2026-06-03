package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.dao.mapper.InternalSystemConfigMapper;
import com.wwh.home.center.model.entity.InternalSystemConfig;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.InternalSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
}
