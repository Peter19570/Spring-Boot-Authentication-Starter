package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.web.webauthn.api.AuthenticatorAssertionResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;

public record PasskeyLoginRequest(
        @NotNull(message = "Credential is required")
        PublicKeyCredential<AuthenticatorAssertionResponse> credential
) {
}
