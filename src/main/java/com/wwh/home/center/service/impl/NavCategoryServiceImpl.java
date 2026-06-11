package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.home.center.dao.mapper.NavCategoryMapper;
import com.wwh.home.center.model.entity.NavCategory;
import com.wwh.home.center.service.NavCategoryService;
import com.wwh.home.center.service.NavLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NavCategoryServiceImpl implements NavCategoryService {

    @Autowired
    private NavCategoryMapper navCategoryMapper;

    @Autowired
    private NavLinkService navLinkService;

    @Override
    public List<NavCategory> listEnabled() {
        return navCategoryMapper.selectList(new LambdaQueryWrapper<NavCategory>()
                .eq(NavCategory::getStatus, 1)
                .orderByAsc(NavCategory::getSortOrder)
                .orderByAsc(NavCategory::getId));
    }

    @Override
    public List<NavCategory> listAll() {
        return navCategoryMapper.selectList(new LambdaQueryWrapper<NavCategory>()
                .orderByAsc(NavCategory::getSortOrder)
                .orderByAsc(NavCategory::getId));
    }

    @Override
    public void addCategory(NavCategory category) {
        LocalDateTime now = LocalDateTime.now();
        category.setId(null);
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        category.setCreateTime(now);
        category.setUpdateTime(now);
        navCategoryMapper.insert(category);
        log.info("新增导航分组成功，name={}", category.getName());
    }

    @Override
    public void updateCategory(NavCategory category) {
        category.setUpdateTime(LocalDateTime.now());
        navCategoryMapper.updateById(category);
        log.info("更新导航分组成功，id={}", category.getId());
    }

    @Override
    public void deleteCategory(Long id) {
        navLinkService.deleteByCategoryId(id);
        navCategoryMapper.deleteById(id);
        log.info("删除导航分组成功，id={}", id);
    }
}
