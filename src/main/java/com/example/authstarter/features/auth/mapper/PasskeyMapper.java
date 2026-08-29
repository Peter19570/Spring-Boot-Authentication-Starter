package com.example.authstarter.features.auth.mapper;

import com.example.authstarter.features.auth.dto.response.PasskeyResponse;
import com.example.authstarter.features.auth.model.Passkey;
import org.mapstruct.*;
import org.springframework.security.web.webauthn.api.CredentialRecord;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = { UUID.class, StandardCharsets.class }
)
public interface PasskeyMapper {

    @Mapping(
            target = "credentialId",
            expression = "java(credentialRecord.getCredentialId().toBase64UrlString())"
    )
    @Mapping(
            target = "publicKey",
            expression = "java(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(credentialRecord.getPublicKey().getBytes()))"
    )
    @Mapping(
            target = "signatureCount",
            expression = "java(credentialRecord.getSignatureCount())"
    )
    @Mapping(
            target = "userId",
            expression = "java(UUID.fromString(new String(credentialRecord.getUserEntityUserId().getBytes(), StandardCharsets.UTF_8)))"
    )
    @Mapping(
            target = "attestationObject",
            expression = "java(credentialRecord.getAttestationObject() != null ? credentialRecord.getAttestationObject().toBase64UrlString() : null)"
    )
    @Mapping(
            target = "attestationClientDataJSON",
            expression = "java(credentialRecord.getAttestationClientDataJSON() != null ? credentialRecord.getAttestationClientDataJSON().toBase64UrlString() : null)"
    )
    Passkey toEntity(CredentialRecord credentialRecord);

    List<PasskeyResponse> toDto(List<Passkey> passkey);

    @Mapping(
            target = "credentialId",
            expression = "java(credentialRecord.getCredentialId().toBase64UrlString())"
    )
    @Mapping(
            target = "publicKey",
            expression = "java(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(credentialRecord.getPublicKey().getBytes()))"
    )
    @Mapping(
            target = "signatureCount",
            expression = "java(credentialRecord.getSignatureCount())"
    )
    @Mapping(
            target = "userId",
            expression = "java(UUID.fromString(new String(credentialRecord.getUserEntityUserId().getBytes(), StandardCharsets.UTF_8)))"
    )
    @Mapping(
            target = "attestationObject",
            expression = "java(credentialRecord.getAttestationObject() != null ? credentialRecord.getAttestationObject().toBase64UrlString() : null)"
    )
    @Mapping(
            target = "attestationClientDataJSON",
            expression = "java(credentialRecord.getAttestationClientDataJSON() != null ? credentialRecord.getAttestationClientDataJSON().toBase64UrlString() : null)"
    )
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CredentialRecord credentialRecord, @MappingTarget Passkey passkey);

}
