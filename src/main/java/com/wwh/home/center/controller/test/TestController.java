package com.wwh.home.center.controller.test;

import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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


    @GetMapping("/images/**")
    public String pathVar(HttpServletRequest request) {
        String uri = request.getRequestURI();
        System.out.println(uri);
        String path = uri.substring(uri.indexOf("images/")+7);
        System.out.println(path);
        return "路径参数为：" + path;
    }
}
