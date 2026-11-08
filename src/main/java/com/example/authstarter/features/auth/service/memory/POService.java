package com.example.authstarter.features.auth.service.memory;

import com.example.authstarter.features.shared.exceptions.custom.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.webauthn.api.AuthenticatorAssertionResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasskeyOptionsService { // Passkey challenges, kept in memory instead of HttpSession

    private final Cache<String, PublicKeyCredentialCreationOptions> creationOptionsStore;
    private final Cache<String, PublicKeyCredentialRequestOptions> requestOptionsStore;
    private final ObjectMapper objectMapper;

    public void saveRegistration(UUID userId, PublicKeyCredentialCreationOptions options) {
        creationOptionsStore.put(userId.toString(), options);
    }

    public PublicKeyCredentialCreationOptions takeRegistration(UUID userId) {
        // remove() is atomic: a challenge can only be used once
        PublicKeyCredentialCreationOptions options = creationOptionsStore.asMap().remove(userId.toString());

        if (options == null) {
            throw new ValidationException("Registration session missing or expired");
        }
        return options;
    }

    public void saveAuthentication(PublicKeyCredentialRequestOptions options) {
        requestOptionsStore.put(options.getChallenge().toBase64UrlString(), options);
    }

    public PublicKeyCredentialRequestOptions takeAuthentication(
            PublicKeyCredential<AuthenticatorAssertionResponse> credential) {

        PublicKeyCredentialRequestOptions options =
                requestOptionsStore.asMap().remove(extractChallenge(credential));

        if (options == null) {
            throw new ValidationException("Login session missing or expired");
        }
        return options;
    }

    private String extractChallenge(PublicKeyCredential<AuthenticatorAssertionResponse> credential) {
        try {
            return objectMapper
                    .readTree(credential.getResponse().getClientDataJSON().getBytes())
                    .path("challenge")
                    .asText();
        } catch (IOException e) {
            throw new ValidationException("Invalid passkey response");
        }
    }
}