package com.example.authstarter.features.auth.service.memory;

import com.example.authstarter.features.auth.exceptions.NotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PRTService { // Password-Reset Token

    private final Cache<String, String> prtStore;

    public String generatePRT(String userId){
        String token = UUID.randomUUID().toString();
        prtStore.put(token, userId);
        return token;
    }

    public String validatePRT(String token){
        String userId = prtStore.getIfPresent(token);

        if (userId == null){
            throw new NotFoundException("Invalid or expired otpToken");
        }

        prtStore.invalidate(token);
        return userId;
    }
}
