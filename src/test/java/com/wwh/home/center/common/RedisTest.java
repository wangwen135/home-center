package com.wwh.home.center.common;

import com.wwh.home.center.HomeCenterApp;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 引入redis
 *
 * @author wangwh
 * @date 2023/11/08
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HomeCenterApp.class)
public class RedisTest {
    @Autowired
    //@Qualifier("redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("genericRedisTemplate")
    private RedisTemplate<String, Object> genericRedisTemplate;


    @Autowired
    @Qualifier("jdkRedisTemplate")
    private RedisTemplate<String, Object> jdkRedisTemplate;

    @Test
    public void testRedis() {
        log.info("设置Redis值");
        redisTemplate.opsForValue().set("appTest", "this is a test message");
        log.info("获取值:{}", redisTemplate.opsForValue().get("appTest"));
    }

    @Test
    public void testGenericRedis() {
        log.info("设置Redis值");
        genericRedisTemplate.opsForValue().set("genericKey", "this is a test message 通用序列化");
        log.info("获取值:{}", genericRedisTemplate.opsForValue().get("genericKey"));

    }

    @Test
    public void testJdkRedis() {
        log.info("设置Redis值");
        jdkRedisTemplate.opsForValue().set("jdkKey", "this is a test message JDK 的序列化");
        log.info("获取值:{}", jdkRedisTemplate.opsForValue().get("jdkKey"));
    }
}
