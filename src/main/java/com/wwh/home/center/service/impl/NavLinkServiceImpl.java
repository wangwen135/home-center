package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.home.center.common.util.ImgUtils;
import com.wwh.home.center.dao.mapper.NavLinkMapper;
import com.wwh.home.center.model.entity.NavLink;
import com.wwh.home.center.service.ImageService;
import com.wwh.home.center.service.NavLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NavLinkServiceImpl implements NavLinkService {

    private static final String DEFAULT_OPEN_TYPE = "blank";

    @Autowired
    private NavLinkMapper navLinkMapper;

    @Autowired
    private ImageService imageService;

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
        List<NavLink> links = navLinkMapper.selectList(wrapper);
        // 公开接口返回时，把图片相对路径拼成可访问 URL；emoji 字段原样返回
        links.forEach(link -> link.setIcon(ImgUtils.formatImagePath(link.getIcon())));
        return links;
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
        if (link.getOpenType() == null) {
            link.setOpenType(DEFAULT_OPEN_TYPE);
        }
        link.setCreateTime(now);
        link.setUpdateTime(now);
        navLinkMapper.insert(link);
        log.info("新增导航链接成功，title={}", link.getTitle());
    }

    @Override
    public void updateLink(NavLink link) {
        if (link.getOpenType() == null) {
            link.setOpenType(DEFAULT_OPEN_TYPE);
        }
        // 图标替换或清空时，清理旧的本地图片文件
        NavLink old = link.getId() == null ? null : navLinkMapper.selectById(link.getId());
        String oldIcon = old == null ? null : old.getIcon();
        link.setUpdateTime(LocalDateTime.now());
        navLinkMapper.updateById(link);
        if (ImgUtils.isLocalStoredPath(oldIcon)
                && !equalsValue(oldIcon, link.getIcon())) {
            imageService.deleteQuietly(oldIcon);
        }
        log.info("更新导航链接成功，id={}", link.getId());
    }

    @Override
    public void deleteLink(Long id) {
        NavLink old = navLinkMapper.selectById(id);
        if (old != null && ImgUtils.isLocalStoredPath(old.getIcon())) {
            imageService.deleteQuietly(old.getIcon());
        }
        navLinkMapper.deleteById(id);
        log.info("删除导航链接成功，id={}", id);
    }

    @Override
    public void deleteByCategoryId(Long categoryId) {
        List<NavLink> links = navLinkMapper.selectList(new LambdaQueryWrapper<NavLink>()
                .eq(NavLink::getCategoryId, categoryId));
        links.forEach(link -> {
            if (ImgUtils.isLocalStoredPath(link.getIcon())) {
                imageService.deleteQuietly(link.getIcon());
            }
        });
        navLinkMapper.delete(new LambdaQueryWrapper<NavLink>()
                .eq(NavLink::getCategoryId, categoryId));
        log.info("删除分组下导航链接成功，categoryId={}", categoryId);
    }

    private static boolean equalsValue(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
