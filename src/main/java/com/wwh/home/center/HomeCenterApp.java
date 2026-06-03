package com.wwh.home.center;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author WWH
 * @version 1.0
 * @date 2022/11/20 21:43
 */
@EnableCaching
@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan("com.wwh.home.center.dao.mapper")
public class HomeCenterApp {

    public static void main(String[] args) {
        SpringApplication.run(HomeCenterApp.class, args);
    }
}
