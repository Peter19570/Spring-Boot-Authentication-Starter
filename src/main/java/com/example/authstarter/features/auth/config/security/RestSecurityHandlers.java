package com.example.authstarter.features.auth.config.security;

import com.example.authstarter.features.auth.config.jwt.JwtFilter;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.example.authstarter.features.auth.constants.SecurityConstants.UNAUTHORIZED_MSG;

@Component
@RequiredArgsConstructor
public class RestSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException ex
    ) throws IOException {
        Object reason = request.getAttribute(JwtFilter.AUTH_ERROR_ATTRIBUTE);
        write(response, HttpStatus.UNAUTHORIZED, reason != null ? reason.toString() : UNAUTHORIZED_MSG);
    }

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex
    ) throws IOException {
        write(response, HttpStatus.FORBIDDEN, "Access denied");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error(status.name(), message)));
    }
}