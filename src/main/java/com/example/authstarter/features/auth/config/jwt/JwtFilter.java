package com.example.authstarter.features.auth.config.jwt;

import com.example.authstarter.features.auth.dto.internal.JwtClaims;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static com.example.authstarter.features.auth.constants.JwtConstants.*;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String jwt = getTokenFromRequest(request);

        if (OPTIONS_HTTP_METHOD.equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtClaims jwtClaims = jwtService.extractToken(jwt);

            CustomUserPrincipal principal = CustomUserPrincipal.fromToken(
                    jwtClaims.userId(), jwtClaims.email(), jwtClaims.authorities());

            if (jwtService.isTokenValid(jwtClaims.expired())) {
                if (!jwtClaims.tokenType().equals(ACCESS_VALUE)){
                    throw new IllegalStateException("Invalid token type. Access token required.");
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | UsernameNotFoundException | IllegalStateException e) {
            handleException(response, "Unauthorized: " + e.getMessage());
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {

        // Extract Token From Header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }

        // Extract Token From Cookie
        if (request.getCookies() != null) {
            return Stream.of(request.getCookies())
                    .filter(cookie -> ACCESS_VALUE.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    private void handleException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("Error", message)));
    }
}