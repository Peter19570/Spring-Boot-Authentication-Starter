package com.example.authstarter.features.auth.config.security;

import com.example.authstarter.features.auth.config.jwt.JwtFilter;
import com.example.authstarter.features.auth.constants.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${spring.application.name}")
    private final String applicationName;

    @Value("${app.frontend.url}")
    private final String frontendUrl;

    @Value("${app.frontend.id}")
    private final String frontendId;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity httpSecurity,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource configurationSource) throws Exception{
        return httpSecurity
                .cors(cors -> cors.configurationSource(configurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, SecurityConstants.PATTERN_URLS).permitAll()
                        .requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()
                        .requestMatchers(SecurityConstants.SWAGGER_URLS).permitAll()
                        .requestMatchers(SecurityConstants.WEBSOCKET_URLS).permitAll()
                        .requestMatchers(SecurityConstants.ACTUATOR_URLS).permitAll()
                        .anyRequest().authenticated())
                // Will come back to the passkeys later (when I get better understanding)
//                .webAuthn(webAuth -> webAuth
//                        .rpName(applicationName)
//                        .rpId(frontendId)
//                        .allowedOrigins(frontendUrl)
//                        .disableDefaultRegistrationPage(true))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }
}

