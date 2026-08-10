package com.example.authstarter.features.auth.config.ratelimit;

import com.example.authstarter.features.auth.constants.RateLimitConstants;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.example.authstarter.features.auth.constants.RateLimitConstants.MAX_ATTEMPTS;
import static com.example.authstarter.features.auth.constants.RateLimitConstants.WINDOW;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> bucketStore;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!shouldRateLimit(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientIdentifier(request);
        Bucket bucket = bucketStore.get(clientId, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            sendRateLimitResponse(response);
        }
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(MAX_ATTEMPTS,
                        Refill.greedy(MAX_ATTEMPTS, WINDOW)))
                .build();
    }

    private boolean shouldRateLimit(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(RateLimitConstants.RATE_LIMITED_ENDPOINTS)
                .anyMatch(path::startsWith);
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        long retryAfter = WINDOW.toSeconds();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                String.format(
                        "{\"msg\":\"Too many attempts. Please try again in %d seconds\",\"retryAfter\":%d}",
                        retryAfter,
                        retryAfter
                )
        );
    }
}

