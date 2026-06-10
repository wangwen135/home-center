package com.wwh.home.center.controller.backend;

import com.wwh.home.center.common.constant.RegexpConstants;
import com.wwh.home.center.common.exception.ForbiddenException;
import com.wwh.home.center.common.model.PageInfo;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.common.util.PageHelper;
import com.wwh.home.center.model.entity.UserInfo;
import com.wwh.home.center.model.qo.PageQuery;
import com.wwh.home.center.model.qo.UserQuery;
import com.wwh.home.center.model.vo.UserInfoVo;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.UserService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Slf4j
@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/backend/user")
@Validated
public class UserManageController {

    @Autowired
    private UserService userService;

    @ApiOperation("管理员重置指定用户密码")
    @ApiImplicitParam(name = "userId", value = "用户id", required = true)
    @PutMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestParam @NotNull(message = "用户Id不能为空") Long userId) {
        checkSuperAdmin();
        log.info("管理员重置用户密码，userId={}", userId);
        userService.resetPassword(userId);
        return Result.success();
    }

    @ApiOperation("根据userId获取用户信息")
    @ApiImplicitParam(name = "userId", value = "用户id", required = true)
    @GetMapping("/getById")
    public Result<UserInfoVo> getById(@RequestParam @NotNull(message = "用户Id不能为空") Long userId) {
        log.debug("根据用户id：{} 查询用户信息", userId);
        UserInfo u = userService.getById(userId);
        return Result.success(convert2Vo(u));
    }

    @ApiOperation("根据电话号码查找用户")
    @ApiImplicitParam(name = "phone", value = "电话号码", required = true)
    @GetMapping("/getByPhone")
    public Result<UserInfoVo> getByPhone(@RequestParam @NotEmpty(message = "电话号码不能为空") String phone) {
        log.debug("根据电话号码：{} 查找用户", phone);
        UserInfo u = userService.getByPhone(phone);
        return Result.success(convert2Vo(u));
    }

    private UserInfoVo convert2Vo(UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, vo);
        return vo;
    }

    @ApiOperation(value = "判断电话号码是否存在")
    @ApiImplicitParam(name = "phone", value = "手机号码", required = true)
    @ApiResponses({@ApiResponse(code = 200, message = "true 表示存在，false 表示不存在")})
    @GetMapping("/existPhone")
    public Result<Boolean> existPhone(
            @RequestParam @NotEmpty(message = "手机号码不能为空") @Pattern(regexp = RegexpConstants.PHONE_NUMBER_REGEXP, message = "手机号码格式不正确") String phone) {
        return Result.success(userService.existPhone(phone));
    }

    @ApiOperation("分页查询用户，表单传参")
    @PostMapping("/findPage")
    public Result<PageInfo<UserInfoVo>> findUserPage(@ApiParam("分页信息") PageQuery page,
                                                     @ApiParam("查询条件") UserQuery query) {
        log.debug("分页查询条件：{}", query);
        PageInfo<UserInfoVo> pageInfo = PageHelper.pageQuery2PageInfo(page);
        return Result.success(userService.findUserPage(query, pageInfo));
    }

    @ApiOperation("分页查询用户2，JSON传参")
    @PostMapping("/findPage2")
    public Result<PageInfo<UserInfoVo>> findUserPage2(@RequestBody PageQuery<UserQuery> pageQuery) {
        log.debug("分页查询条件：{}", pageQuery);
        PageQuery pq = new PageQuery();
        pq.setCondition(null);
        PageInfo<UserInfoVo> pageInfo = PageHelper.pageQuery2PageInfo(pageQuery);
        return Result.success(userService.findUserPage(pageQuery.getCondition(), pageInfo));
    }

    @ApiOperation("分页查询测试")
    @PostMapping("/unionAllTest")
    public Result<PageInfo<UserInfoVo>> unionAllTest(@RequestBody PageQuery page) {
        log.debug("分页查询条件：{}", page);
        return Result.success(userService.unionAllTest(page.getPageNum(), page.getPageSize()));
    }

    private void checkSuperAdmin() {
        if (!UserContextHolder.isSuperAdmin()) {
            throw new ForbiddenException("只有超级管理员才能访问后台管理接口");
        }
    }

}
