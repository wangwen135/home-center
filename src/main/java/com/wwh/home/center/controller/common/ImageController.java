package com.wwh.home.center.controller.common;

import com.wwh.home.center.common.constant.ResultConstants;
import com.wwh.home.center.common.exception.ArgumentException;
import com.wwh.home.center.common.model.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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
        String[] allowedExtensions = {"jpg", "jpeg", "png", "gif"};
        for (String extension : allowedExtensions) {
            if (fileName.toLowerCase().endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    @ApiOperation("查看图片")
    @GetMapping("/view/{imageName}")
    public ResponseEntity<byte[]> viewImage(@ApiParam("图片路径") @PathVariable String imageName) {
        try {
            // 构建文件路径
            String filePath = imageBasePath + File.separator + imageName;
            File file = new File(filePath);

            if (!file.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // 读取文件内容
            byte[] fileContent = FileUtils.readFileToByteArray(file);

            return ResponseEntity.ok().body(fileContent);
        } catch (IOException e) {
            log.error("返回图片异常", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
