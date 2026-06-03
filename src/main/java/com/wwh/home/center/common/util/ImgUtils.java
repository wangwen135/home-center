package com.wwh.home.center.common.util;

import com.wwh.home.center.common.constant.SysConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * 图片工具
 *
 * @author wangwh
 * @date 2024/02/07
 */
public class ImgUtils {

    /**
     * 格式化图片路径
     *
     * @param path
     * @return
     */
    public static String formatImagePath(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return SysConstants.IMAGE_URL_PREFIX + path;
    }
}
