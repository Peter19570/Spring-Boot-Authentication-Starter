package com.example.authstarter.features.auth.config.cors;

import com.example.authstarter.features.auth.constants.CorsConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(CorsConstants.ALLOWED_ORIGINS);
        config.setAllowedMethods(CorsConstants.ALLOWED_METHODS);
        config.setAllowedHeaders(CorsConstants.ALLOWED_HEADERS);
        config.setExposedHeaders(CorsConstants.ALLOWED_EXPOSED_HEADERS);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
