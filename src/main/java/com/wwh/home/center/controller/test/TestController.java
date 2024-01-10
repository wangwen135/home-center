package com.wwh.home.center.controller.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * TODO
 *
 * @author WWH
 * @version 1.0
 * @date 2022/11/20 21:46
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/demo")
    public String demo() {
        return "访问demo：" + LocalDateTime.now().toString();
    }


    private final String UPLOAD_DIR = "/path/to/upload/directory";

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 获取文件名和文件大小
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();

        // 创建上传目录（如果不存在）
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 保存文件到服务器
        File dest = new File(uploadDir, fileName);
        file.transferTo(dest);

        return ResponseEntity.ok("File uploaded successfully");
    }
}
