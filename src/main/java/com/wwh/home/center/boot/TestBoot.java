package com.wwh.home.center.boot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 程序启动时执行
 *
 * @author wangwh
 * @date 2023/04/26
 */
@Slf4j
@Component
@Order(1)
public class TestBoot implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("程序启动后执行");
    }
}
