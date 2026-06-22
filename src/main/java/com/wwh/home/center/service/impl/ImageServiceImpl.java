package com.wwh.home.center.service.impl;

import com.wwh.home.center.common.exception.ArgumentException;
import com.wwh.home.center.dao.mapper.ImageRecordMapper;
import com.wwh.home.center.model.entity.ImageRecord;
import com.wwh.home.center.security.UserContextHolder;
import com.wwh.home.center.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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
     * 单文件大小上限：10MB
     */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

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

    /**
     * SVG 危险内容特征，命中任一即拒绝上传：
     * <ul>
     *     <li>{@code <script} 脚本块</li>
     *     <li>{@code javascript:} 协议</li>
     *     <li>{@code onXxx=} 事件属性（onload、onclick 等）</li>
     *     <li>{@code data:} 内嵌脚本数据</li>
     *     <li>{@code <foreignObject}、{@code <iframe}、{@code <embed} 等可承载 HTML 的标签</li>
     *     <li>外链资源引用（http(s)/ftp 的 use/xlink:href）</li>
     * </ul>
     */
    private static final Pattern[] SVG_DANGEROUS_PATTERNS = new Pattern[]{
            Pattern.compile("<\\s*script", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\son[a-z0-9_\\-]+\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("data\\s*:[^,]*text", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*foreignObject", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*iframe", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*embed", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(href|xlink:href)\\s*=\\s*[\"']\\s*(https?|ftp)://", Pattern.CASE_INSENSITIVE)
    };

    @Value("${image.base-path}")
    private String imageBasePath;

    @Autowired
    private ImageRecordMapper imageRecordMapper;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ArgumentException("上传文件为空");
        }
        String fileName = file.getOriginalFilename();
        log.debug("上传的文件为：{}", fileName);

        String extension = extensionOf(fileName);
        if (extension == null) {
            throw new ArgumentException("只能上传图片文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ArgumentException("文件大小超过限制（最大10MB）");
        }
        // SVG 作为活动内容，必须做安全校验
        if ("svg".equals(extension) && !isSafeSvg(file)) {
            throw new ArgumentException("SVG 文件包含不安全内容（脚本/事件属性/外链资源），已拒绝上传");
        }

        String mediaType = MEDIA_TYPES.get(extension);
        long fileSize = file.getSize();
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

            // 记录图片元数据，便于审计和文件清理
            saveImageRecord(filePath, fileName, fileSize, mediaType);
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

    @Override
    public void deleteQuietly(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return;
        }
        // 外链地址不处理
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return;
        }
        try {
            File target = resolve(relativePath);
            if (target.exists() && !target.delete()) {
                log.warn("删除图片文件失败：{}", relativePath);
            }
        } catch (Exception e) {
            log.warn("删除图片文件异常：path={}", relativePath, e);
        }
    }

    private void saveImageRecord(String relativePath, String originalName, long fileSize, String mimeType) {
        try {
            ImageRecord record = new ImageRecord();
            record.setRelativePath(relativePath);
            record.setOriginalName(originalName);
            record.setFileSize(fileSize);
            record.setMimeType(mimeType);
            record.setUploadTime(LocalDateTime.now());
            // 上传通常在登录后，未登录场景留空
            record.setUploadUserId(UserContextHolder.getUserId());
            imageRecordMapper.insert(record);
        } catch (Exception e) {
            // 元数据记录失败不影响主流程，仅记录日志
            log.warn("保存图片元数据失败：path={}", relativePath, e);
        }
    }

    /**
     * SVG 安全校验：命中任一危险特征即视为不安全。
     */
    private boolean isSafeSvg(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            for (Pattern pattern : SVG_DANGEROUS_PATTERNS) {
                if (pattern.matcher(content).find()) {
                    log.warn("SVG 文件命中危险特征 {}，已拒绝上传", pattern);
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            log.warn("读取 SVG 内容失败，按不安全处理", e);
            return false;
        }
    }

    /**
     * 取小写扩展名，不在白名单内返回 null。
     */
    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        String ext = fileName.substring(dot + 1).toLowerCase();
        return MEDIA_TYPES.containsKey(ext) ? ext : null;
    }

    /**
     * 判断文件类型是否为支持的图片
     */
    public static boolean isImageFile(String fileName) {
        return extensionOf(fileName) != null;
    }

    private static String generateRandomFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}
