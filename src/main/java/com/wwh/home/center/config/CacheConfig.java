package com.wwh.home.center.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * 缓存配置，使用Caffeine
 *
 * @author wangwh
 * @date 2021-7-30
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 30分钟自动失效缓存管理器
     *
     * @return
     */
    @Primary
    @Bean("autoExpireCacheManager")
    public CacheManager cacheManager() {
        // Caffeine配置
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                // 一次缓存10分钟
                .expireAfterWrite(10, TimeUnit.MINUTES)
                // 缓存的最大条数
                .maximumSize(10000);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

}
