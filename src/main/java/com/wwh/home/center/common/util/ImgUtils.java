package com.wwh.home.center.common.util;

import com.wwh.home.center.common.constant.SysConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 图片工具
 *
 * @author wangwh
 * @date 2024/02/07
 */
public class ImgUtils {

    /**
     * 本地存储相对路径特征：形如 {@code 2024/01/uuid.png}（不含协议、不以 / 开头、以图片后缀结尾）
     */
    private static final Pattern LOCAL_STORED_PATTERN = Pattern.compile(
            "^[^/\\s]+/[^\\s]+\\.(png|jpe?g|gif|webp|svg)$", Pattern.CASE_INSENSITIVE);

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

    /**
     * 判断是否为本地存储的相对图片路径（用于图标替换/删除时的旧文件清理）。
     *
     * <p>外链地址（http/https）、emoji、空值都返回 false。</p>
     *
     * @param path 图标字段值
     * @return 是否本地相对路径
     */
    public static boolean isLocalStoredPath(String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("/")) {
            return false;
        }
        return LOCAL_STORED_PATTERN.matcher(path).matches();
    }
}
