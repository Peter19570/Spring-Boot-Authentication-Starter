package com.example.authstarter.features.auth.config.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
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
import java.util.Map;
import java.util.stream.Stream;

import static com.example.authstarter.features.auth.constants.RateLimitConstants.*;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final Cache<String, Bucket> bucketStore;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String matchedEndpoint = matchEndpoint(request.getRequestURI());
        if (matchedEndpoint == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientIdentifier(request);
        String bucketKey = matchedEndpoint + ":" + clientId;

        Bucket bucket = bucketStore.get(bucketKey, k -> createBucketFor(matchedEndpoint));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            sendRateLimitResponse(response, probe.getNanosToWaitForRefill());
        }
    }

    private Bucket createBucketFor(String endpoint) {
        return Bucket.builder()
                .addLimit(ENDPOINT_LIMITS.get(endpoint))
                .build();
    }

    private String matchEndpoint(String path) {
        return Stream.of(RATE_LIMITED_ENDPOINTS)
                .filter(path::startsWith)
                .findFirst()
                .orElse(null);
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, long retryAfter) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        Map.of("message", "Too many requests", "retryAfter", retryAfter))
        );
    }
}

