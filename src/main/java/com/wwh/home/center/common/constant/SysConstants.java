package com.wwh.home.center.common.constant;

import java.nio.charset.Charset;

/**
 * 系统常量
 *
 * @author wangwh
 * @date 2023/06/20
 */
public class SysConstants {
    /**
     * 默认编码
     */
    public static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARACTER_ENCODING);

    public static final String TOKEN_NAME = "token";
    public static final String COOKIE_TOKEN_NAME = "wwh_token";

    /**
     * 超级管理员角色ID
     */
    public static final int SUPER_ADMIN_ROLE_ID = 1;

    /**
     * 超级管理员用户ID
     */
    //public static final int SUPER_ADMIN_USER_ID = 1;


    /**
     * 图片前缀
     */
    public static final String IMAGE_URL_PREFIX = "/common/img/view/";

    private SysConstants() {

    }
}
