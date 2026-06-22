package com.wwh.home.center.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Token 信息
 *
 * @author wangwh
 * @date 2024/01/26
 */
@ApiModel(description = "Token信息")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenVo {

    @ApiModelProperty("token")
    private String token;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @ApiModelProperty("过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expirationTime;

    @ApiModelProperty("最长有效截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime maxExpirationTime;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("登录IP")
    private String loginIp;

    @ApiModelProperty("最近访问IP")
    private String lastAccessIp;

    @ApiModelProperty("最近访问时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastAccessTime;

    @ApiModelProperty("User-Agent")
    private String userAgent;

    @ApiModelProperty("触发登录的原始目标地址")
    private String originalUri;

    @ApiModelProperty("会话状态：online/kicked/logged_out/expired")
    private String status;


}
