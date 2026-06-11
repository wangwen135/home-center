package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.home.center.dao.mapper.NavLinkMapper;
import com.wwh.home.center.model.entity.NavLink;
import com.wwh.home.center.service.NavLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NavLinkServiceImpl implements NavLinkService {

    @Autowired
    private NavLinkMapper navLinkMapper;

    @Override
    public List<NavLink> listEnabled(Long categoryId) {
        LambdaQueryWrapper<NavLink> wrapper = new LambdaQueryWrapper<NavLink>()
                .eq(NavLink::getStatus, 1)
                .orderByAsc(NavLink::getCategoryId)
                .orderByAsc(NavLink::getSortOrder)
                .orderByAsc(NavLink::getId);
        if (categoryId != null) {
            wrapper.eq(NavLink::getCategoryId, categoryId);
        }
        return navLinkMapper.selectList(wrapper);
    }

    @Override
    public List<NavLink> listAll(Long categoryId) {
        LambdaQueryWrapper<NavLink> wrapper = new LambdaQueryWrapper<NavLink>()
                .orderByAsc(NavLink::getCategoryId)
                .orderByAsc(NavLink::getSortOrder)
                .orderByAsc(NavLink::getId);
        if (categoryId != null) {
            wrapper.eq(NavLink::getCategoryId, categoryId);
        }
        return navLinkMapper.selectList(wrapper);
    }

    @Override
    public void addLink(NavLink link) {
        LocalDateTime now = LocalDateTime.now();
        link.setId(null);
        if (link.getStatus() == null) {
            link.setStatus(1);
        }
        if (link.getSortOrder() == null) {
            link.setSortOrder(0);
        }
        link.setCreateTime(now);
        link.setUpdateTime(now);
        navLinkMapper.insert(link);
        log.info("新增导航链接成功，title={}", link.getTitle());
    }

    @Override
    public void updateLink(NavLink link) {
        link.setUpdateTime(LocalDateTime.now());
        navLinkMapper.updateById(link);
        log.info("更新导航链接成功，id={}", link.getId());
    }

    @Override
    public void deleteLink(Long id) {
        navLinkMapper.deleteById(id);
        log.info("删除导航链接成功，id={}", id);
    }

    @Override
    public void deleteByCategoryId(Long categoryId) {
        navLinkMapper.delete(new LambdaQueryWrapper<NavLink>()
                .eq(NavLink::getCategoryId, categoryId));
        log.info("删除分组下导航链接成功，categoryId={}", categoryId);
    }
}
