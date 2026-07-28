package com.example.authstarter.features.auth.config.cache;

import com.example.authstarter.features.auth.constants.CacheConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(){
        return new ConcurrentMapCacheManager(CacheConstants.CACHE_NAMES);
    }
}
