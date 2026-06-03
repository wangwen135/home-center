package com.wwh.home.center.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author wwh
 * @since 2024-01-08
 */
@Data
@TableName("user_info")
@ApiModel(value = "UserInfo对象", description = "用户信息")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("性别 1 男性 2 女性")
    private Integer gender;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("随机加密盐值")
    private String salt;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("手机号码")
    private String phone;

    @ApiModelProperty("邮箱地址")
    private String email;

    @ApiModelProperty("0 正常 1 已禁用")
    private Boolean disabled;

    @ApiModelProperty("0 正常 1 已锁定")
    private Boolean locked;

    @ApiModelProperty("0 正常 1 已过期")
    private Boolean expired;

    @ApiModelProperty("过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expirationTime;

    @ApiModelProperty("0 正常 1 已删除")
    private Boolean deleted;

    @ApiModelProperty("创建人")
    private Integer createBy;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @ApiModelProperty("修改人")
    private Integer updateBy;

    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;


}
