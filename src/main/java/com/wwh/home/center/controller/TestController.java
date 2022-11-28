package com.wwh.home.center.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/demo")
    public String demo() {
        return "访问demo：" + LocalDateTime.now().toString();
    }
}
