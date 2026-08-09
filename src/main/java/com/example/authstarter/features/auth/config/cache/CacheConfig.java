package com.example.authstarter.features.auth.config.cache;

import com.example.authstarter.features.auth.constants.CacheConstants;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager handleObjectCache() {
        CaffeineCacheManager manager = new CaffeineCacheManager(CacheConstants.CACHE_NAMES);
        manager.setCaffeine(
                Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .maximumSize(10_000)
        );
        return manager;
    }

    @Bean
    public Cache<String, String> handleOtpCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(10_000)
                .build();
    }
}
