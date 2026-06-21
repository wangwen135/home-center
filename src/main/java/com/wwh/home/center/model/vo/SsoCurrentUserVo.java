package com.wwh.home.center.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class SsoCurrentUserVo {

    private Integer userId;

    private String username;

    private String nickname;

    private String avatar;

    private List<String> roles;

    private List<String> permissions;
}
