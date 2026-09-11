package com.example.authstarter.features.auth.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static com.example.authstarter.features.auth.constants.AsyncConstants.*;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = EMAIL_EXECUTOR)
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(EMAIL_THREAD);
        executor.initialize();
        return executor;
    }
}
