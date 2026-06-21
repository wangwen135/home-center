package com.wwh.home.center.controller.common;

import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.PrivateNavCategory;
import com.wwh.home.center.model.entity.PrivateNavLink;
import com.wwh.home.center.service.PrivateNavCategoryService;
import com.wwh.home.center.service.PrivateNavLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
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
@Validated
@Api(tags = "私有导航接口")
@RestController
@RequestMapping("/api/private-nav")
public class PrivateNavController {

    @Autowired
    private PrivateNavCategoryService privateNavCategoryService;

    @Autowired
    private PrivateNavLinkService privateNavLinkService;

    @ApiOperation("当前用户启用私有导航分组")
    @GetMapping("/categories")
    public Result<List<PrivateNavCategory>> categories() {
        return Result.success(privateNavCategoryService.listCurrentUserEnabled());
    }

    @ApiOperation("当前用户启用私有导航入口")
    @GetMapping("/links")
    public Result<List<PrivateNavLink>> links(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(privateNavLinkService.listCurrentUserEnabled(categoryId));
    }

    @ApiOperation("当前用户全部私有导航数据")
    @GetMapping("/all")
    public Result<PrivateNavAllResponse> all() {
        PrivateNavAllResponse response = new PrivateNavAllResponse();
        response.setCategories(privateNavCategoryService.listCurrentUserEnabled());
        response.setLinks(privateNavLinkService.listCurrentUserEnabled(null));
        return Result.success(response);
    }

    @ApiOperation("当前用户私有导航分组管理列表")
    @GetMapping("/manage/categories")
    public Result<List<PrivateNavCategory>> manageCategories() {
        return Result.success(privateNavCategoryService.listCurrentUserAll());
    }

    @ApiOperation("当前用户私有导航入口管理列表")
    @GetMapping("/manage/links")
    public Result<List<PrivateNavLink>> manageLinks(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(privateNavLinkService.listCurrentUserAll(categoryId));
    }

    @ApiOperation("新增当前用户私有导航分组")
    @PostMapping("/category")
    public Result<Void> addCategory(@RequestBody PrivateNavCategory category) {
        privateNavCategoryService.addCurrentUserCategory(category);
        return Result.success();
    }

    @ApiOperation("修改当前用户私有导航分组")
    @PutMapping("/category")
    public Result<Void> updateCategory(@RequestBody PrivateNavCategory category) {
        privateNavCategoryService.updateCurrentUserCategory(category);
        return Result.success();
    }

    @ApiOperation("删除当前用户私有导航分组")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable @NotNull(message = "分组ID不能为空") Long id) {
        privateNavCategoryService.deleteCurrentUserCategory(id);
        return Result.success();
    }

    @ApiOperation("新增当前用户私有导航入口")
    @PostMapping("/link")
    public Result<Void> addLink(@RequestBody PrivateNavLink link) {
        privateNavLinkService.addCurrentUserLink(link);
        return Result.success();
    }

    @ApiOperation("修改当前用户私有导航入口")
    @PutMapping("/link")
    public Result<Void> updateLink(@RequestBody PrivateNavLink link) {
        privateNavLinkService.updateCurrentUserLink(link);
        return Result.success();
    }

    @ApiOperation("删除当前用户私有导航入口")
    @DeleteMapping("/link/{id}")
    public Result<Void> deleteLink(@PathVariable @NotNull(message = "入口ID不能为空") Long id) {
        privateNavLinkService.deleteCurrentUserLink(id);
        return Result.success();
    }

    @Data
    public static class PrivateNavAllResponse {
        private List<PrivateNavCategory> categories;
        private List<PrivateNavLink> links;
    }
}
