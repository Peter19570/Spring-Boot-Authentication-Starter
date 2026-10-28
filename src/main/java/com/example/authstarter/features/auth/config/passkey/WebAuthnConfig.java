package com.example.authstarter.features.auth.config.passkey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;

import java.util.Set;

@Configuration
public class WebAuthnConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.webauthn.rp-id}")
    private String rpId;

    @Value("${app.webauthn.allowed-origins}")
    private Set<String> allowedOrigins;

    @Bean
    public WebAuthnRelyingPartyOperations relyingPartyOperations(
            PublicKeyCredentialUserEntityRepository userEntities,
            UserCredentialRepository userCredentials) {

        return new Webauthn4JRelyingPartyOperations(
                userEntities,
                userCredentials,
                PublicKeyCredentialRpEntity.builder().id(rpId).name(appName).build(),
                allowedOrigins
        );
    }

    @Bean
    public WebauthnJacksonModule webauthnJacksonModule() {
        return new WebauthnJacksonModule();
    }
}
