package com.example.authstarter.features.auth.dto.request;

import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;

public record PasskeyRegistrationRequest(
        String label,
        PublicKeyCredential<AuthenticatorAttestationResponse> credential
) {
}
