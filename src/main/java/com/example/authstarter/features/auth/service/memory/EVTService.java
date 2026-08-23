package com.example.authstarter.features.auth.service.memory;

import com.example.authstarter.features.auth.dto.internal.Verification;
import com.example.authstarter.features.auth.exceptions.NotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EVTService { // Email Verification Token

    private final Cache<String, Verification> evtStore;

    public String generateEVT(String userId, String newEmail){
        String token = UUID.randomUUID().toString();
        evtStore.put(token, Verification.data(userId, newEmail));
        return token;
    }

    public Verification validateEVT(String token){
        Verification userData = evtStore.getIfPresent(token);

        if (userData == null){
            throw new NotFoundException("Invalid or expired otpToken");
        }

        evtStore.invalidate(token);
        return userData;
    }
}
