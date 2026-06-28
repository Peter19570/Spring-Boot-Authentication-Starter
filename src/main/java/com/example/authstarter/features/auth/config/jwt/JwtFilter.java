package com.example.authstarter.features.auth.config.jwt;

import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

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

        try {
            UUID userId = UUID.fromString(jwtService.extractUserId(jwt));
            String email = jwtService.extractUserEmail(jwt); // this is low-key not useful (at least to me)
            List<String> rawRoles = jwtService.extractUserRoles(jwt);

            List<SimpleGrantedAuthority> authorities = rawRoles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            CustomUserPrincipal principal = new CustomUserPrincipal(userId, email, authorities);

            if (jwtService.isTokenValid(jwt, principal.id().toString())) {

                if (jwtService.extractTokenType(jwt).equals("refresh")){
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
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // And this forr token in the cookie, for now I don't issue the token to the client via cookie
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    private void handleException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}