package com.wwh.home.center.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 登录结果
 *
 * @author wangwh
 * @date 2024/01/08
 */
@ApiModel("登录结果")
@Data
public class LoginVo {

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("token")
    private String token;
}
