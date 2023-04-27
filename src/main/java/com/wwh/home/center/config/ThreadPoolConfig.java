package com.wwh.home.center.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

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

    @Value("${threadpool.corePoolSize:2}")
    private int corePoolSize;
    @Value("${threadpool.maxPoolSize:5}")
    private int maxPoolSize;
    @Value("${threadpool.queueCapacity:0}")
    private int queueCapacity;

    @Bean("handleExecutor")
    public Executor postChainExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("handle-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

    @Bean("taskExecutor")
    public TaskScheduler taskExecutor() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix("taskScheduled-");
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        // threadPoolTaskScheduler.setAwaitTerminationSeconds(30);
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }
}
