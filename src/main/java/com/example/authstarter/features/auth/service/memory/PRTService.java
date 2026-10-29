package com.example.authstarter.features.auth.service.memory;

import com.example.authstarter.features.shared.exceptions.custom.NotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

import static com.example.authstarter.features.auth.service.helpers.AuthHelper.hashToken;
import static com.example.authstarter.features.shared.utils.TokenUtils.newToken;

@Component
@RequiredArgsConstructor
public class PRTService { // Password-Reset Token

    private final SecureRandom random;
    private final Cache<String, String> prtStore;

    public String generatePRT(String userId){
        String token = newToken(random);
        prtStore.put(hashToken(token), userId);
        return token;
    }

    public String validatePRT(String token){
        String userId = token == null ? null : prtStore.asMap().remove(hashToken(token));

        if (userId == null){
            throw new NotFoundException("Invalid or expired reset token");
        }

        return userId;
    }
}
