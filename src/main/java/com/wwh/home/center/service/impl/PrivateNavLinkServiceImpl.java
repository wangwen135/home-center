package com.wwh.home.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wwh.home.center.common.util.ImgUtils;
import com.wwh.home.center.dao.mapper.PrivateNavLinkMapper;
import com.wwh.home.center.model.entity.PrivateNavLink;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.ImageService;
import com.wwh.home.center.service.PrivateNavLinkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PrivateNavLinkServiceImpl implements PrivateNavLinkService {

    private static final String DEFAULT_ENTRY_TYPE = "link";
    private static final String DEFAULT_OPEN_TYPE = "blank";

    @Autowired
    private PrivateNavLinkMapper privateNavLinkMapper;

    @Autowired
    private ImageService imageService;

    @Override
    public List<PrivateNavLink> listCurrentUserEnabled(Long categoryId) {
        LambdaQueryWrapper<PrivateNavLink> wrapper = baseWrapper(categoryId)
                .eq(PrivateNavLink::getStatus, 1)
                .orderByAsc(PrivateNavLink::getCategoryId)
                .orderByAsc(PrivateNavLink::getSortOrder)
                .orderByAsc(PrivateNavLink::getId);
        List<PrivateNavLink> links = privateNavLinkMapper.selectList(wrapper);
        links.forEach(link -> link.setIcon(ImgUtils.formatImagePath(link.getIcon())));
        return links;
    }

    @Override
    public List<PrivateNavLink> listCurrentUserAll(Long categoryId) {
        LambdaQueryWrapper<PrivateNavLink> wrapper = baseWrapper(categoryId)
                .orderByAsc(PrivateNavLink::getCategoryId)
                .orderByAsc(PrivateNavLink::getSortOrder)
                .orderByAsc(PrivateNavLink::getId);
        return privateNavLinkMapper.selectList(wrapper);
    }

    @Override
    public void addCurrentUserLink(PrivateNavLink link) {
        LocalDateTime now = LocalDateTime.now();
        link.setId(null);
        link.setUserId(currentUserId());
        applyDefaults(link);
        link.setCreateTime(now);
        link.setUpdateTime(now);
        privateNavLinkMapper.insert(link);
        log.info("新增私有导航入口成功，userId={}, title={}", link.getUserId(), link.getTitle());
    }

    @Override
    public void updateCurrentUserLink(PrivateNavLink link) {
        Integer userId = currentUserId();
        link.setUserId(null);
        link.setCreateTime(null);
        applyDefaults(link);
        // 图标替换或清空时，清理旧的本地图片文件
        PrivateNavLink old = privateNavLinkMapper.selectOne(new LambdaQueryWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getId, link.getId())
                .eq(PrivateNavLink::getUserId, userId));
        String oldIcon = old == null ? null : old.getIcon();
        link.setUpdateTime(LocalDateTime.now());
        privateNavLinkMapper.update(link, new LambdaUpdateWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getId, link.getId())
                .eq(PrivateNavLink::getUserId, userId));
        if (ImgUtils.isLocalStoredPath(oldIcon)
                && !equalsValue(oldIcon, link.getIcon())) {
            imageService.deleteQuietly(oldIcon);
        }
        log.info("更新私有导航入口成功，userId={}, id={}", userId, link.getId());
    }

    @Override
    public void deleteCurrentUserLink(Long id) {
        Integer userId = currentUserId();
        PrivateNavLink old = privateNavLinkMapper.selectOne(new LambdaQueryWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getId, id)
                .eq(PrivateNavLink::getUserId, userId));
        if (old != null && ImgUtils.isLocalStoredPath(old.getIcon())) {
            imageService.deleteQuietly(old.getIcon());
        }
        privateNavLinkMapper.delete(new LambdaQueryWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getId, id)
                .eq(PrivateNavLink::getUserId, userId));
        log.info("删除私有导航入口成功，userId={}, id={}", userId, id);
    }

    @Override
    public void deleteCurrentUserByCategoryId(Long categoryId) {
        Integer userId = currentUserId();
        List<PrivateNavLink> links = privateNavLinkMapper.selectList(new LambdaQueryWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getCategoryId, categoryId)
                .eq(PrivateNavLink::getUserId, userId));
        links.forEach(link -> {
            if (ImgUtils.isLocalStoredPath(link.getIcon())) {
                imageService.deleteQuietly(link.getIcon());
            }
        });
        privateNavLinkMapper.delete(new LambdaQueryWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getCategoryId, categoryId)
                .eq(PrivateNavLink::getUserId, userId));
    }

    private static boolean equalsValue(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private LambdaQueryWrapper<PrivateNavLink> baseWrapper(Long categoryId) {
        LambdaQueryWrapper<PrivateNavLink> wrapper = new LambdaQueryWrapper<PrivateNavLink>()
                .eq(PrivateNavLink::getUserId, currentUserId());
        if (categoryId != null) {
            wrapper.eq(PrivateNavLink::getCategoryId, categoryId);
        }
        return wrapper;
    }

    private void applyDefaults(PrivateNavLink link) {
        if (link.getStatus() == null) {
            link.setStatus(1);
        }
        if (link.getSortOrder() == null) {
            link.setSortOrder(0);
        }
        if (StringUtils.isBlank(link.getEntryType())) {
            link.setEntryType(DEFAULT_ENTRY_TYPE);
        }
        if (StringUtils.isBlank(link.getOpenType())) {
            link.setOpenType(DEFAULT_OPEN_TYPE);
        }
    }

    private Integer currentUserId() {
        return UserContextHolder.isLoggedIn();
    }
}
