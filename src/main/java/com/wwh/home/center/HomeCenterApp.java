package com.wwh.home.center;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author WWH
 * @version 1.0
 * @date 2022/11/20 21:43
 */
@SpringBootApplication
@MapperScan("com.wwh.home.center.dao.mapper")
public class HomeCenterApp {

    public static void main(String[] args) {
        SpringApplication.run(HomeCenterApp.class, args);
    }
}
