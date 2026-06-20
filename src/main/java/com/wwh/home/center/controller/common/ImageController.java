package com.wwh.home.center.controller.common;

import com.wwh.home.center.common.exception.ResourceNotFoundException;
import com.wwh.home.center.common.model.Result;
import com.wwh.home.center.service.ImageService;
import com.wwh.home.center.service.impl.ImageServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * 图片
 *
 * @author wangwh
 * @date 2024/01/26
 */
@Slf4j
@RestController
@Api(tags = "图片相关")
@RequestMapping("/common/img")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @ApiOperation("上传图片")
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(imageService.upload(file));
    }

    @ApiOperation("查看图片")
    @GetMapping("/view/**")
    public ResponseEntity<Resource> viewImage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.debug("查看图片请求路径是：{}", uri);
        String imgPath = uri.substring(uri.indexOf("view/") + 5);

        if (!ImageServiceImpl.isImageFile(imgPath)) {
            log.debug("请求后缀不合法：{}", imgPath);
            throw new ResourceNotFoundException("请求后缀不合法：" + imgPath);
        }

        // 构建文件路径
        File file = imageService.resolve(imgPath);

        if (!file.exists()) {
            log.debug("文件不存在：{}", file.getAbsolutePath());
            throw new ResourceNotFoundException("文件不存在：" + imgPath);
        }

        // 确定文件扩展名
        String extension = StringUtils.getFilenameExtension(file.getName()).toLowerCase();
        // 获取对应的MediaType
        String mediaType = ImageServiceImpl.MEDIA_TYPES.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaType))
                .lastModified(file.lastModified()).body(resource);
    }
}
