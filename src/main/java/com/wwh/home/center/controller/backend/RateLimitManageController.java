package com.wwh.home.center.controller.backend;

import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.vo.RateLimitStatusVo;
import com.wwh.home.center.security.CheckAuthRateLimiter;
import com.wwh.home.center.security.UserContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * /checkAuth 限流后台管理（纯内存）
 *
 * @author wangwh
 */
@Slf4j
@Api(tags = "限流管理")
@Validated
@RestController
@RequestMapping("/backend/rate-limit")
public class RateLimitManageController {

    @Autowired
    private CheckAuthRateLimiter checkAuthRateLimiter;

    @ApiOperation("全部 IP 的限流状态")
    @GetMapping("/status")
    public Result<List<RateLimitStatusVo>> status() {
        checkSuperAdmin();
        return Result.success(checkAuthRateLimiter.listStatus());
    }

    @ApiOperation("释放单个 IP（清空计数与封禁）")
    @DeleteMapping("/ip/{ip}")
    public Result<Boolean> release(@PathVariable @NotBlank(message = "IP不能为空") String ip) {
        checkSuperAdmin();
        return Result.success(checkAuthRateLimiter.release(ip));
    }

    @ApiOperation("释放全部 IP")
    @PostMapping("/reset")
    public Result<Void> releaseAll() {
        checkSuperAdmin();
        checkAuthRateLimiter.releaseAll();
        return Result.success();
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能管理限流");
        }
    }
}
