package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.NavLink;

import java.util.List;

public interface NavLinkService {

    List<NavLink> listEnabled(Long categoryId);

    List<NavLink> listAll(Long categoryId);

    void addLink(NavLink link);

    void updateLink(NavLink link);

    void deleteLink(Long id);

    void deleteByCategoryId(Long categoryId);
}
