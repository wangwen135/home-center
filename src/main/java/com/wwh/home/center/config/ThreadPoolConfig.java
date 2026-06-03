package com.wwh.home.center.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author wangwh
 * @date 2021-3-15
 */
@EnableAsync
@Configuration
public class ThreadPoolConfig {

    @Value("${threadPool.corePoolSize:4}")
    private int corePoolSize;
    @Value("${threadPool.maxPoolSize:8}")
    private int maxPoolSize;
    @Value("${threadPool.queueCapacity:30}")
    private int queueCapacity;
    @Value("${threadPool.keepAliveSeconds:300}")
    private int keepAliveSeconds;

    // 将这个线程池设为默认线程池
    @Primary
    @Bean("handleExecutor")
    public Executor handleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("handle-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

    @Bean("singleTaskExecutor")
    public Executor singleTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Single-");
        executor.initialize();
        return executor;
    }

}
