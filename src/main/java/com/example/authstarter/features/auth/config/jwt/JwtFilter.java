package com.example.authstarter.features.auth.config.jwt;

import com.example.authstarter.features.auth.constants.JWTConstants;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

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

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getMethod().equals(JWTConstants.HTTP_REQUEST_METHOD)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = UUID.fromString(jwtService.extractUserId(jwt));
            String email = jwtService.extractUserEmail(jwt); // (unnecessary btw since i work with userID)
            List<String> rawRoles = jwtService.extractUserRoles(jwt);

            List<SimpleGrantedAuthority> authorities = rawRoles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            CustomUserPrincipal principal = new CustomUserPrincipal(userId, email, authorities);

            if (jwtService.isTokenValid(jwt, principal.id().toString())) {
                if (!jwtService.extractTokenType(jwt).equals("at")){
                    throw new IllegalStateException("Invalid token type. Access token required.");
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            filterChain.doFilter(request, response);

        } catch (JwtException | UsernameNotFoundException | IllegalStateException e) {
            handleException(response, "Unauthorized: " + e.getMessage());
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {

        // This for token in the header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(JWTConstants.TOKEN_PREFIX)) {
            return authHeader.substring(7);
        }

        // And this forr token in the cookie, for now I don't issue the token to the client via cookie
        if (request.getCookies() != null) {
            return Stream.of(request.getCookies())
                    .filter(cookie -> "at".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    private void handleException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
    }
}