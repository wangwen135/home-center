package com.wwh.home.center.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 图片存储服务
 *
 * <p>统一封装图片上传校验与落盘逻辑，供 {@code /common/img/upload}、
 * 用户头像等场景复用。</p>
 *
 * @author wangwh
 * @date 2024/01/26
 */
public interface ImageService {

    /**
     * 上传图片，落盘到 {@code image.base-path} 下的 {@code yyyy/MM} 子目录。
     *
     * <p>校验文件类型白名单（PNG/JPG/JPEG/GIF/WEBP/SVG）和大小上限（10MB），
     * SVG 作为活动内容会做安全校验，包含脚本、事件属性或外链资源时拒绝上传。</p>
     *
     * @param file 上传的文件
     * @return 相对路径，例如 {@code 2024/01/xxxx.png}
     */
    String upload(MultipartFile file);

    /**
     * 根据相对路径解析对应的磁盘文件（不判断文件是否存在）。
     *
     * @param relativePath 相对路径，例如 {@code 2024/01/xxxx.png}
     * @return 磁盘文件
     */
    File resolve(String relativePath);

    /**
     * 静默删除本地存储的图片文件。
     *
     * <p>用于图标替换或删除时的旧文件清理。只处理本地相对路径，
     * 外链地址（http/https）或空值直接忽略；文件不存在或删除失败时仅记录日志，不抛异常。</p>
     *
     * @param relativePath 相对路径，例如 {@code 2024/01/xxxx.png}
     */
    void deleteQuietly(String relativePath);
}
