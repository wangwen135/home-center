package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wwh.home.center.dao.mapper.PrivateNavCategoryMapper;
import com.wwh.home.center.model.entity.PrivateNavCategory;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.PrivateNavCategoryService;
import com.wwh.home.center.service.PrivateNavLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PrivateNavCategoryServiceImpl implements PrivateNavCategoryService {

    @Autowired
    private PrivateNavCategoryMapper privateNavCategoryMapper;

    @Autowired
    private PrivateNavLinkService privateNavLinkService;

    @Override
    public List<PrivateNavCategory> listCurrentUserEnabled() {
        LambdaQueryWrapper<PrivateNavCategory> wrapper = baseWrapper()
                .eq(PrivateNavCategory::getStatus, 1)
                .orderByAsc(PrivateNavCategory::getSortOrder)
                .orderByAsc(PrivateNavCategory::getId);
        return privateNavCategoryMapper.selectList(wrapper);
    }

    @Override
    public List<PrivateNavCategory> listCurrentUserAll() {
        LambdaQueryWrapper<PrivateNavCategory> wrapper = baseWrapper()
                .orderByAsc(PrivateNavCategory::getSortOrder)
                .orderByAsc(PrivateNavCategory::getId);
        return privateNavCategoryMapper.selectList(wrapper);
    }

    @Override
    public void addCurrentUserCategory(PrivateNavCategory category) {
        LocalDateTime now = LocalDateTime.now();
        category.setId(null);
        category.setUserId(currentUserId());
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        category.setCreateTime(now);
        category.setUpdateTime(now);
        privateNavCategoryMapper.insert(category);
        log.info("新增私有导航分组成功，userId={}, name={}", category.getUserId(), category.getName());
    }

    @Override
    public void updateCurrentUserCategory(PrivateNavCategory category) {
        Integer userId = currentUserId();
        category.setUserId(null);
        category.setCreateTime(null);
        category.setUpdateTime(LocalDateTime.now());
        privateNavCategoryMapper.update(category, new LambdaUpdateWrapper<PrivateNavCategory>()
                .eq(PrivateNavCategory::getId, category.getId())
                .eq(PrivateNavCategory::getUserId, userId));
        log.info("更新私有导航分组成功，userId={}, id={}", userId, category.getId());
    }

    @Override
    public void deleteCurrentUserCategory(Long id) {
        Integer userId = currentUserId();
        privateNavLinkService.deleteCurrentUserByCategoryId(id);
        privateNavCategoryMapper.delete(new LambdaQueryWrapper<PrivateNavCategory>()
                .eq(PrivateNavCategory::getId, id)
                .eq(PrivateNavCategory::getUserId, userId));
        log.info("删除私有导航分组成功，userId={}, id={}", userId, id);
    }

    private LambdaQueryWrapper<PrivateNavCategory> baseWrapper() {
        return new LambdaQueryWrapper<PrivateNavCategory>()
                .eq(PrivateNavCategory::getUserId, currentUserId());
    }

    private Integer currentUserId() {
        return UserContextHolder.isLoggedIn();
    }
}
