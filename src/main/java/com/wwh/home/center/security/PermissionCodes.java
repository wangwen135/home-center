package com.wwh.home.center.security;

/**
 * Central permission URL patterns stored in sys_permission.urls.
 */
public final class PermissionCodes {

    public static final String MENU_HOME = "/";
    public static final String MENU_PRIVATE_NAV = "/private.html";
    public static final String MENU_PC_MONITOR = "/device/pc/monitor.html";
    public static final String MENU_PC_POWER = "/device/pc/power.html";
    public static final String MENU_BACKEND = "/admin/manage.html";

    public static final String BACKEND_ALL = "/backend/**";
    public static final String BACKEND_USER = "/backend/user/**";
    public static final String BACKEND_ROLE = "/backend/role/**";
    public static final String BACKEND_PERMISSION = "/backend/permission/**";
    public static final String BACKEND_PC_DEVICE = "/backend/device/pc/**";
    public static final String BACKEND_DATA = "/backend/data/**";

    public static final String DEVICE_PC_VIEW = "/device/pc/devices;/device/pc/permissions;/device/pc/*/screenshot/latest";
    public static final String DEVICE_PC_SCREENSHOT = "/device/pc/*/screenshot";
    public static final String DEVICE_PC_POWER = "/device/pc/power/*/*";
    public static final String DEVICE_PC_WEB_SHELL = "/device/pc/command/*";

    private PermissionCodes() {
    }
}
