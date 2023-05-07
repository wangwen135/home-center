package com.wwh.home.center.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件分段上传，测试
 *
 * @author wangwh
 * @date 2023/05/04
 */
@RestController
public class ChunkedUploadController {

    private final String UPLOAD_DIR = "C:\\tmp";

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("index") int index,
                                         @RequestParam("total") int total) throws IOException {
        // 获取文件名和文件大小
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();

        // 创建上传目录（如果不存在）
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 计算分块文件名
        String chunkFileName = fileName + ".part" + index;

        // 保存分块文件到服务器
        File dest = new File(uploadDir, chunkFileName);
        file.transferTo(dest);

        // 判断是否所有分块都已经上传完毕
        if (index == total - 1) {
            // 合并分块文件
            mergeChunks(fileName, total);
        }

        return ResponseEntity.ok("File uploaded successfully");
    }

    private void mergeChunks(String fileName, int total) throws IOException {
        // 创建目标文件
        File destFile = new File(UPLOAD_DIR, fileName);
        FileOutputStream outputStream = new FileOutputStream(destFile);

        // 依次读取每个分块文件，写入到目标文件中
        for (int i = 0; i < total; i++) {
            String chunkFileName = fileName + ".part" + i;
            File chunkFile = new File(UPLOAD_DIR, chunkFileName);
            FileInputStream inputStream = new FileInputStream(chunkFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            chunkFile.delete();
        }

        outputStream.close();
    }
}
