package com.wwh.home.center.common.enums;

/**
 * 系统日志类型
 *
 * @author wangwh
 * @date 2024/01/10
 */
public enum SysLogTypeEnum {
    LOGIN,//登录
    LOGOUT,//退出
    CHANGE_PWD, //修改密码
    KICK_OUT, //超管踢出会话
    SESSION_INVALIDATED //会话失效（新登录/用户禁用/角色权限变更/密码变更等）
}
