package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.NavCategory;

import java.util.List;

public interface NavCategoryService {

    List<NavCategory> listEnabled();

    List<NavCategory> listAll();

    void addCategory(NavCategory category);

    void updateCategory(NavCategory category);

    void deleteCategory(Long id);
}
