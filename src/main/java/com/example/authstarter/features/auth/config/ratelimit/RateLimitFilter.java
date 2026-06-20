package com.example.authstarter.features.auth.config.ratelimit;

import com.example.authstarter.features.auth.constants.RateLimitConstants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.authstarter.features.auth.constants.RateLimitConstants.MAX_ATTEMPTS;
import static com.example.authstarter.features.auth.constants.RateLimitConstants.WINDOW;

//    =========================================================================================
//    DON'T USE IN PRODUCTION !!! I ALWAYS USE BUCKET 4J WITH REDIS INSTEAD.
//    =========================================================================================

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler =
            Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void init() {
        cleanupScheduler.scheduleAtFixedRate(
                this::cleanupOldBuckets, 10, 10, TimeUnit.MINUTES);
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(MAX_ATTEMPTS,
                        Refill.greedy(MAX_ATTEMPTS, WINDOW)))
                .build();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!shouldRateLimit(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientIdentifier(request);
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            sendRateLimitResponse(response);
        }
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

    private void cleanupOldBuckets() {
        // Remove buckets that haven't been used recently
        // Bucket4J doesn't expose last access time directly
        // You'd need to track this separately
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"msg\": \"Too many attempts. Please try again in %d seconds.\", " +
                        "\"retryAfter\": %d}",
                WINDOW.toSeconds(),
                WINDOW.toSeconds()
        ));
    }

    @PreDestroy
    public void destroy() {
        cleanupScheduler.shutdown();
    }
}

