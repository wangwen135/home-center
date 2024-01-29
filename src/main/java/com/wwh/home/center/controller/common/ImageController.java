package com.wwh.home.center.controller.common;

import com.wwh.home.center.common.constant.ResultConstants;
import com.wwh.home.center.common.exception.ArgumentException;
import com.wwh.home.center.common.exception.ResourceNotFoundException;
import com.wwh.home.center.common.model.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 图片
 *
 * @author wangwh
 * @date 2024/01/26
 */
@Slf4j
@RestController
@Api(tags = "图片相关")
@RequestMapping("/common/images")
public class ImageController {

    @Value("${image.base-path}")
    private String imageBasePath;

    // 支持的图片类型和对应的MediaType
    private static final Map<String, String> MEDIA_TYPES = new HashMap<>();

    static {
        MEDIA_TYPES.put("jpg", MediaType.IMAGE_JPEG_VALUE);
        MEDIA_TYPES.put("jpeg", MediaType.IMAGE_JPEG_VALUE);
        MEDIA_TYPES.put("png", MediaType.IMAGE_PNG_VALUE);
        MEDIA_TYPES.put("gif", MediaType.IMAGE_GIF_VALUE);
        // 添加更多支持的图片类型
    }

    @ApiOperation("上传图片")
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ArgumentException("上传文件为空");
        }
        // 获取文件名
        String fileName = file.getOriginalFilename();
        log.debug("上传的文件为：{}", fileName);

        // 判断文件类型是否为图片
        if (!isImageFile(fileName)) {
            throw new ArgumentException("只能上传图片文件");
        }

        // 判断文件大小是否超过限制（这里设置为2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ArgumentException("文件大小超过限制（最大2MB）");
        }
        try {
            // 生成随机文件名
            String newFileName = generateRandomFileName(fileName);

            String year = LocalDate.now().getYear() + "";
            String month = LocalDate.now().getMonthValue() + "";
            month = month.length() == 1 ? '0' + month : month;

            // 构建上传目标路径
            String filePath = year + File.separator + month + File.separator + newFileName;

            String fullPath = imageBasePath + File.separator + filePath;

            // 创建文件对象
            File dest = new File(fullPath);

            // 检测是否存在目录，不存在则创建
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            // 写入文件
            file.transferTo(dest);

            return Result.success(filePath);
        } catch (IOException e) {
            log.error("上传图片异常", e);
            return Result.error(ResultConstants.SYSTEM_ERROR_CODE, "文件上传异常");
        }

    }

    // 生成随机文件名
    private String generateRandomFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    // 判断文件类型是否为图片
    private boolean isImageFile(String fileName) {
        for (String extension : MEDIA_TYPES.keySet()) {
            if (fileName.toLowerCase().endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    @ApiOperation("查看图片")
    @GetMapping("/view/**")
    public ResponseEntity<Resource> viewImage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.debug("查看图片请求路径是：{}", uri);
        String imgPath = uri.substring(uri.indexOf("view/") + 5);

        if (!isImageFile(imgPath)) {
            log.debug("请求后缀不合法：{}", imgPath);
            //return new ResponseEntity<>( HttpStatus.NOT_FOUND);
            throw new ResourceNotFoundException("请求后缀不合法：" + imgPath);
        }

        // 构建文件路径
        String filePath = imageBasePath + File.separator + imgPath;
        File file = new File(filePath);

        if (!file.exists()) {
            log.debug("文件不存在：{}", filePath);
            throw new ResourceNotFoundException("文件不存在：" + filePath);
        }

        // 确定文件扩展名
        String extension = StringUtils.getFilenameExtension(file.getName()).toLowerCase();
        // 获取对应的MediaType
        String mediaType = MEDIA_TYPES.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaType))
                .lastModified(file.lastModified()).body(resource);
    }
}
