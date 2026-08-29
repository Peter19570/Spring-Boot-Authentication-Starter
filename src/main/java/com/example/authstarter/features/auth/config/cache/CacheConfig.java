package com.example.authstarter.features.auth.config.cache;

import com.example.authstarter.features.auth.constants.CacheConstants;
import com.example.authstarter.features.auth.dto.internal.Verification;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * UTILISING JAVA MEMORY TO STORE CACHE, USE REDIS WHEN SCALING (HORIZONTALLY)
     * */

    @Bean
    public CacheManager dataStore() {
        CaffeineCacheManager manager = new CaffeineCacheManager(CacheConstants.CACHE_NAMES);
        manager.setCaffeine(
                Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .maximumSize(10_000)
        );
        return manager;
    }

    @Bean
    public Cache<String, Verification> evtStore(){
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(20))
                .maximumSize(10_000)
                .build();
    }

    @Bean
    public Cache<String, String> prtStore(){
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .maximumSize(10_100)
                .build();
    }

    @Bean
    public Cache<String, String> otpStore() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(10_000)
                .build();
    }

    @Bean
    public Cache<String, Bucket> bucketStore() {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .maximumSize(100_000)
                .build();
    }
}
