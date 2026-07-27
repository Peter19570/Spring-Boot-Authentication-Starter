package com.example.authstarter.features.auth.config.passkey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.authentication.HttpSessionPublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.authentication.PublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;
import org.springframework.security.web.webauthn.registration.HttpSessionPublicKeyCredentialCreationOptionsRepository;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsRepository;

import java.util.Set;

@Configuration
public class WebAuthnConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.frontend.domain.id}")
    private String domainId;

    @Value("${app.frontend.url}")
    private Set<String> allowedOrigins;

    @Bean
    public WebAuthnRelyingPartyOperations relyingPartyOperations(
            PublicKeyCredentialUserEntityRepository userEntities,
            UserCredentialRepository userCredentials) {

        return new Webauthn4JRelyingPartyOperations(
                userEntities,
                userCredentials,
                PublicKeyCredentialRpEntity.builder()
                        .id(domainId)
                        .name(appName)
                        .build(),
                allowedOrigins
        );
    }

    @Bean
    public PublicKeyCredentialCreationOptionsRepository creationOptionsRepository() {
        return new HttpSessionPublicKeyCredentialCreationOptionsRepository();
    }

    @Bean
    public PublicKeyCredentialRequestOptionsRepository requestOptionsRepository() {
        return new HttpSessionPublicKeyCredentialRequestOptionsRepository();
    }

    @Bean
    public WebauthnJacksonModule webauthnJacksonModule() {
        return new WebauthnJacksonModule();
    }
}
