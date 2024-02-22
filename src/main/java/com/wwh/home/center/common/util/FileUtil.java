package com.wwh.home.center.common.util;

/**
 * 文件工具类
 *
 * @author wangwh
 * @date 2024/02/22
 */
public class FileUtil {
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        // 获取最后一个点的位置
        int lastDotIndex = fileName.lastIndexOf('.');

        // 检查是否存在点并且点不在文件名的开头或结尾
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            // 通过截取字符串获取文件后缀名
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        } else {
            return "";
        }
    }
}
