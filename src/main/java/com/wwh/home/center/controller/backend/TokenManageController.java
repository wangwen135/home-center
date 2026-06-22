package com.wwh.home.center.controller.backend;

import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.vo.TokenVo;
import com.wwh.home.center.security.TokenManager;
import com.wwh.home.center.security.UserContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Token / 在线会话管理
 *
 * @author wangwh
 * @date 2024/01/26
 */
@Slf4j
@Api(tags = "Token管理")
@RestController
@RequestMapping("/backend/token")
@Validated
public class TokenManageController {

    @ApiOperation("全部在线会话信息")
    @GetMapping("/list")
    public Result<List<TokenVo>> list() {
        checkSuperAdmin();
        return Result.success(TokenManager.getAll());
    }

    @ApiOperation("踢出指定用户的当前会话")
    @DeleteMapping("/user/{userId}")
    public Result<Void> kick(@PathVariable @NotNull(message = "用户ID不能为空") Integer userId) {
        checkSuperAdmin();
        TokenManager.kickByUserId(userId, UserContextHolder.getUserId(), UserContextHolder.getUsername());
        return Result.success();
    }

    @ApiOperation("批量踢出选中用户的会话")
    @PostMapping("/kick-batch")
    public Result<Void> kickBatch(@RequestBody List<Integer> userIds) {
        checkSuperAdmin();
        if (userIds != null) {
            Integer operatorId = UserContextHolder.getUserId();
            String operatorName = UserContextHolder.getUsername();
            userIds.forEach(userId -> TokenManager.kickByUserId(userId, operatorId, operatorName));
        }
        return Result.success();
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能管理在线会话");
        }
    }
}

