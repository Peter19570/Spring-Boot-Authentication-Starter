package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;

public record PasskeyRegistrationRequest(
        @Size(max = 100, message = "Device name must not exceed 100 characters")
        String label,

        @NotNull(message = "Credential is required")
        PublicKeyCredential<AuthenticatorAttestationResponse> credential
) {
}
