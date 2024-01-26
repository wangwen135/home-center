package com.wwh.home.center.controller.test;

import io.swagger.annotations.ApiParam;
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


    @GetMapping("/images/{imageName}")
    public String pathVar(@ApiParam("图片路径") @PathVariable String imageName ) {
        return "路径参数为：" + imageName;
    }
}
