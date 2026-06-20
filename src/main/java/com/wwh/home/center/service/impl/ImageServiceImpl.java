package com.wwh.home.center.service.impl;

import com.wwh.home.center.common.exception.ArgumentException;
import com.wwh.home.center.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 图片存储服务实现
 *
 * @author wangwh
 * @date 2024/01/26
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    /**
     * 单文件大小上限：2MB
     */
    private static final long MAX_SIZE = 2 * 1024 * 1024;

    /**
     * 支持的图片类型和对应的MediaType（同时作为允许上传的后缀白名单）
     */
    public static final Map<String, String> MEDIA_TYPES;

    static {
        Map<String, String> types = new HashMap<>();
        types.put("jpg", MediaType.IMAGE_JPEG_VALUE);
        types.put("jpeg", MediaType.IMAGE_JPEG_VALUE);
        types.put("png", MediaType.IMAGE_PNG_VALUE);
        types.put("gif", MediaType.IMAGE_GIF_VALUE);
        types.put("webp", "image/webp");
        types.put("svg", "image/svg+xml");
        MEDIA_TYPES = Collections.unmodifiableMap(types);
    }

    @Value("${image.base-path}")
    private String imageBasePath;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ArgumentException("上传文件为空");
        }
        String fileName = file.getOriginalFilename();
        log.debug("上传的文件为：{}", fileName);

        if (!isImageFile(fileName)) {
            throw new ArgumentException("只能上传图片文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ArgumentException("文件大小超过限制（最大2MB）");
        }

        try {
            String newFileName = generateRandomFileName(fileName);

            LocalDate now = LocalDate.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());

            // 构建相对路径：yyyy/MM/uuid.ext
            String filePath = year + File.separator + month + File.separator + newFileName;

            File dest = new File(imageBasePath + File.separator + filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
            return filePath;
        } catch (IOException e) {
            log.error("上传图片异常", e);
            throw new ArgumentException("文件上传异常");
        }
    }

    /**
     * 判断文件类型是否为支持的图片
     */
    @Override
    public File resolve(String relativePath) {
        File base = new File(imageBasePath);
        File target = new File(base, relativePath);
        // 防目录穿越：解析后必须仍在 base 目录内（view 接口已公开，必须校验）
        try {
            String canonicalBase = base.getCanonicalPath();
            String canonicalTarget = target.getCanonicalPath();
            if (!canonicalTarget.equals(canonicalBase) && !canonicalTarget.startsWith(canonicalBase + File.separator)) {
                throw new ArgumentException("非法的图片路径");
            }
        } catch (IOException e) {
            throw new ArgumentException("非法的图片路径");
        }
        return target;
    }

    /**
     * 判断文件类型是否为支持的图片
     */
    public static boolean isImageFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        for (String extension : MEDIA_TYPES.keySet()) {
            if (lower.endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    private static String generateRandomFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}
