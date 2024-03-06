package com.wwh.home.center.common.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件工具类
 *
 * @author wangwh
 * @date 2024/02/22
 */
@Slf4j
public class FileUtil {


    /**
     * 计算有效的文件名称
     *
     * @param path
     * @param name
     * @return
     */
    public static String calcValidFileName(File path, String name) {
        String[] names = path.list();
        if (names.length == 0) {
            return name;
        }
        Set<String> nameSet = Stream.of(names).collect(Collectors.toSet());
        String namePart, suffix;
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex == -1) {
            namePart = name;
            suffix = "";
        } else {
            namePart = name.substring(0, lastDotIndex);
            suffix = name.substring(lastDotIndex);
        }

        String newName = name;
        int count = 1;
        while (nameSet.contains(newName)) {
            newName = namePart + count + suffix;
            count++;
        }
        return newName;
    }

    /**
     * 计算有效的目录名称
     *
     * @param path
     * @param name
     * @return
     */
    public static String calcValidDirName(File path, String name) {
        String[] names = path.list();
        if (names.length == 0) {
            return name;
        }
        Set<String> nameSet = Stream.of(names).collect(Collectors.toSet());
        String newName = name;
        int suffix = 1;
        while (nameSet.contains(newName)) {
            newName = name + suffix;
            suffix++;
        }
        return newName;
    }

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

    @Data
    public static class FileTimeInfo {
        private Date creationTime;
        private Date lastModifiedTime;
    }

    /**
     * 获取文件时间信息
     *
     * @param file
     * @return
     */
    public static FileTimeInfo getFileTimeInfo(File file) {
        if (file == null) {
            return null;
        }
        FileTimeInfo fti = new FileTimeInfo();
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            FileTime creationTime = attrs.creationTime();
            FileTime lastModifiedTime = attrs.lastModifiedTime();

            fti.setCreationTime(new Date(creationTime.toMillis()));
            fti.setLastModifiedTime(new Date(lastModifiedTime.toMillis()));

        } catch (Exception e) {
            log.error("Error reading file[{}] attributes", file.getPath(), e);
        }

        return fti;
    }


    public static void main1(String[] args) {
        Path path = Paths.get("D:\\temp\\ShutdownTest.jar");
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime creationTime = attrs.creationTime();
            FileTime lastModifiedTime = attrs.lastModifiedTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("创建时间：" + sdf.format(new Date(creationTime.toMillis())));
            System.out.println("修改时间：" + sdf.format(new Date(lastModifiedTime.toMillis())));
        } catch (Exception e) {
            System.err.println("Error reading file attributes: " + e.getMessage());
        }
    }
}
