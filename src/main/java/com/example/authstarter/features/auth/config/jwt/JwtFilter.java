package com.example.authstarter.features.auth.config.jwt;

import com.example.authstarter.features.auth.dto.internal.JwtClaims;
import com.example.authstarter.features.shared.dto.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Stream;

import static com.example.authstarter.features.auth.constants.JwtConstants.*;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /** Request attribute read by RestSecurityHandlers to explain a 401. */
    public static final String AUTH_ERROR_ATTRIBUTE = "auth.error";

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return OPTIONS_HTTP_METHOD.equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = getTokenFromRequest(request);

        if (jwt != null) {
            authenticate(jwt, request);
        }

        // Never reject here: public endpoints must work even with a stale token.
        // Protected endpoints get a 401 from the authentication entry point.
        filterChain.doFilter(request, response);
    }

    private void authenticate(String jwt, HttpServletRequest request) {
        try {
            JwtClaims claims = jwtService.extractToken(jwt);

            if (!ACCESS_VALUE.equals(claims.tokenType())) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Invalid token type. Access token required.");
                return;
            }

            UserPrincipal principal = UserPrincipal.fromToken(
                    claims.userId(), claims.email(), claims.authorities());

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (ExpiredJwtException e) {
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Token has expired");
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Invalid token");
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
}