package com.wwh.home.center.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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

    /**
     * redis缓存管理器，默认十分钟过期
     *
     * @param connectionFactory
     * @return
     */
    @Bean("autoExpireRedisCacheManager")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))// 设置缓存失效时间为十分钟
                .prefixCacheNameWith("ac:")
                //.prefixKeysWith("ac:")
                //序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 配置缓存名为"hoursCache"的缓存，设置失效时间为1小时
        cacheConfigurations.put("hoursCache", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith("ac_hours:")
                //.prefixKeysWith("ac_hours:")
                //序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
        );

        // 配置缓存名为"daysCache"的缓存，设置失效时间为1天
        cacheConfigurations.put("daysCache", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .prefixCacheNameWith("ac_days:")
                //.prefixKeysWith("ac_days:")
                //序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
        );
        //其他缓存....

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

}
