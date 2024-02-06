package com.wwh.home.center.controller;

import com.wwh.home.center.common.exception.UnauthorizedException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.security.LoginManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;

/**
 * 登录
 *
 * @author wangwh
 * @date 2024/01/08
 */
@Slf4j
@Validated
@Api(tags = "登录")
@RestController
public class LoginController {

    @Autowired
    private LoginManager loginManager;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<String> login(@RequestParam @NotEmpty(message = "用户名不能为空") @ApiParam("用户名或手机号") String username
            , @RequestParam @NotEmpty(message = "密码不能为空") @ApiParam("密码") String password
            , HttpServletRequest request, HttpServletResponse response) {

        return Result.success(loginManager.login(username, password, response));
    }

    @ApiOperation("退出")
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            loginManager.logout(response);
        } catch (UnauthorizedException e) {
            log.debug("用户没有登录");
        }
        response.sendRedirect("/login.html");
        return Result.success();
    }
}
