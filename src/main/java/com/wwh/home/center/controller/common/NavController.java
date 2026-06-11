package com.wwh.home.center.controller.common;

import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.entity.NavCategory;
import com.wwh.home.center.model.entity.NavLink;
import com.wwh.home.center.service.NavCategoryService;
import com.wwh.home.center.service.NavLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "公开导航接口")
@RestController
@RequestMapping("/api/nav")
public class NavController {

    @Autowired
    private NavCategoryService navCategoryService;

    @Autowired
    private NavLinkService navLinkService;

    @ApiOperation("启用导航分组")
    @GetMapping("/categories")
    public Result<List<NavCategory>> categories() {
        return Result.success(navCategoryService.listEnabled());
    }

    @ApiOperation("启用导航链接")
    @GetMapping("/links")
    public Result<List<NavLink>> links(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(navLinkService.listEnabled(categoryId));
    }

    @ApiOperation("全部启用导航数据")
    @GetMapping("/all")
    public Result<NavAllResponse> all() {
        NavAllResponse response = new NavAllResponse();
        response.setCategories(navCategoryService.listEnabled());
        response.setLinks(navLinkService.listEnabled(null));
        return Result.success(response);
    }

    @Data
    public static class NavAllResponse {
        private List<NavCategory> categories;
        private List<NavLink> links;
    }
}
