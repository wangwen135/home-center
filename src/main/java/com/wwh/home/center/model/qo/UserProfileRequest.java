package com.wwh.home.center.model.qo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 当前用户自助修改个人资料的请求体
 *
 * @author wangwh
 */
@Data
@ApiModel(description = "个人资料修改请求")
public class UserProfileRequest {

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("性别 1：男 2：女")
    private Integer gender;

    @ApiModelProperty("手机号码")
    private String phone;

    @ApiModelProperty("邮箱地址")
    private String email;
}
