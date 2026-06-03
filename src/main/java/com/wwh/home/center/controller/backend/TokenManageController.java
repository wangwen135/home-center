package com.wwh.home.center.controller.backend;

import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.model.vo.TokenVo;
import com.wwh.home.center.security.TokenManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Token 管理
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

    @ApiOperation("全部token信息")
    @GetMapping("/list")
    public Result<List<TokenVo>> list() {
        return Result.success(TokenManager.getAll());
    }
}
