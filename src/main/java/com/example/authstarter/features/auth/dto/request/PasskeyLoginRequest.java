package com.example.authstarter.features.auth.dto.request;

import org.springframework.security.web.webauthn.api.AuthenticatorAssertionResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;

public record PasskeyLoginRequest(
        PublicKeyCredential<AuthenticatorAssertionResponse> credential
) {
}
