package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.PrivateNavLink;

import java.util.List;

public interface PrivateNavLinkService {

    List<PrivateNavLink> listCurrentUserEnabled(Long categoryId);

    List<PrivateNavLink> listCurrentUserAll(Long categoryId);

    void addCurrentUserLink(PrivateNavLink link);

    void updateCurrentUserLink(PrivateNavLink link);

    void deleteCurrentUserLink(Long id);

    void deleteCurrentUserByCategoryId(Long categoryId);
}
