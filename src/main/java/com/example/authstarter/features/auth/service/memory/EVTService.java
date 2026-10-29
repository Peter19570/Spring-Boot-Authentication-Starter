package com.example.authstarter.features.auth.service.memory;

import com.example.authstarter.features.auth.dto.internal.Verification;
import com.example.authstarter.features.shared.exceptions.custom.NotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

import static com.example.authstarter.features.auth.service.helpers.AuthHelper.hashToken;
import static com.example.authstarter.features.shared.utils.TokenUtils.newToken;

@Component
@RequiredArgsConstructor
public class EVTService { // Email Verification Token

    private final SecureRandom random;
    private final Cache<String, Verification> evtStore;

    public String generateEVT(String userId, String newEmail){
        String token = newToken(random);
        evtStore.put(hashToken(token), Verification.data(userId, newEmail));
        return token;
    }

    public Verification validateEVT(String token){
        // remove() is atomic: two concurrent requests can't both consume the same token
        Verification userData = token == null ? null : evtStore.asMap().remove(hashToken(token));

        if (userData == null){
            throw new NotFoundException("Invalid or expired verification token");
        }

        return userData;
    }
}
