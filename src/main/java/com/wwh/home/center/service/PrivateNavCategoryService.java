package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.PrivateNavCategory;

import java.util.List;

public interface PrivateNavCategoryService {

    List<PrivateNavCategory> listCurrentUserEnabled();

    List<PrivateNavCategory> listCurrentUserAll();

    void addCurrentUserCategory(PrivateNavCategory category);

    void updateCurrentUserCategory(PrivateNavCategory category);

    void deleteCurrentUserCategory(Long id);
}
