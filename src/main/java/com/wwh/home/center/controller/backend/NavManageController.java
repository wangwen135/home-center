package com.wwh.home.center.controller.backend;

import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.NavCategory;
import com.wwh.home.center.model.entity.NavLink;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.NavCategoryService;
import com.wwh.home.center.service.NavLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Api(tags = "导航后台管理接口")
@Validated
@RestController
@RequestMapping("/backend/nav")
public class NavManageController {

    @Autowired
    private NavCategoryService navCategoryService;

    @Autowired
    private NavLinkService navLinkService;

    @ApiOperation("分组列表")
    @GetMapping("/categories")
    public Result<List<NavCategory>> categories() {
        checkSuperAdmin();
        return Result.success(navCategoryService.listAll());
    }

    @ApiOperation("新增分组")
    @PostMapping("/category")
    public Result<Void> addCategory(@RequestBody NavCategory category) {
        checkSuperAdmin();
        navCategoryService.addCategory(category);
        return Result.success();
    }

    @ApiOperation("修改分组")
    @PutMapping("/category")
    public Result<Void> updateCategory(@RequestBody NavCategory category) {
        checkSuperAdmin();
        navCategoryService.updateCategory(category);
        return Result.success();
    }

    @ApiOperation("删除分组")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable @NotNull(message = "分组ID不能为空") Long id) {
        checkSuperAdmin();
        navCategoryService.deleteCategory(id);
        return Result.success();
    }

    @ApiOperation("链接列表")
    @GetMapping("/links")
    public Result<List<NavLink>> links(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        checkSuperAdmin();
        return Result.success(navLinkService.listAll(categoryId));
    }

    @ApiOperation("新增链接")
    @PostMapping("/link")
    public Result<Void> addLink(@RequestBody NavLink link) {
        checkSuperAdmin();
        navLinkService.addLink(link);
        return Result.success();
    }

    @ApiOperation("修改链接")
    @PutMapping("/link")
    public Result<Void> updateLink(@RequestBody NavLink link) {
        checkSuperAdmin();
        navLinkService.updateLink(link);
        return Result.success();
    }

    @ApiOperation("删除链接")
    @DeleteMapping("/link/{id}")
    public Result<Void> deleteLink(@PathVariable @NotNull(message = "链接ID不能为空") Long id) {
        checkSuperAdmin();
        navLinkService.deleteLink(id);
        return Result.success();
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能访问后台管理接口");
        }
    }
}
