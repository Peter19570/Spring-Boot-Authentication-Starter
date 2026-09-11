package com.example.authstarter.features.auth.config.cors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.example.authstarter.features.auth.constants.CorsConstants.*;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setExposedHeaders(ALLOWED_EXPOSED_HEADERS);
        config.setAllowCredentials(true);
        config.setMaxAge(MAX_AGE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(PATTERN, config);
        return source;
    }
}
