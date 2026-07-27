package com.example.authstarter.features.auth.config.passkey;

import com.example.authstarter.features.auth.mapper.PasskeyMapper;
import com.example.authstarter.features.auth.model.Passkey;
import com.example.authstarter.features.auth.repo.PasskeyRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import org.springframework.security.web.webauthn.api.*;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasskeyConfig implements UserCredentialRepository {

    private final PasskeyRepo passkeyRepo;
    private final PasskeyMapper passkeyMapper;

    @Override
    public void delete(Bytes credentialId) {
        String extractedCredentialId = credentialId.toBase64UrlString();
        passkeyRepo.deleteByCredentialId(extractedCredentialId);
    }

    @Override
    public void save(CredentialRecord credentialRecord) {
        String credentialId = credentialRecord.getCredentialId().toBase64UrlString();

        Passkey passkey = passkeyRepo.findByCredentialId(credentialId)
                .orElseGet(() -> passkeyMapper.toEntity(credentialRecord));

        passkeyMapper.updateEntity(credentialRecord, passkey);

        passkeyRepo.save(passkey);
    }

    @Override
    public @Nullable CredentialRecord findByCredentialId(Bytes credentialId) {
        String extractedCredentialId = credentialId.toBase64UrlString();
        return passkeyRepo.findByCredentialId(extractedCredentialId)
                .map(this::toCredentialRecord)
                .orElse(null);
    }

    @Override
    public List<CredentialRecord> findByUserId(Bytes userId) {
        UUID extractedUserId = UUID.fromString(new String(userId.getBytes(), StandardCharsets.UTF_8));
        return passkeyRepo.findAllByUserId(extractedUserId)
                .stream().map(this::toCredentialRecord)
                .toList();
    }

    private CredentialRecord toCredentialRecord(Passkey passkey) {
        Bytes publicKeyBytes = Bytes.fromBase64(passkey.getPublicKey());
        PublicKeyCose publicKeyObj = new ImmutablePublicKeyCose(publicKeyBytes.getBytes());
        Bytes userIdInBytes = new Bytes(passkey.getUserId().toString().getBytes(StandardCharsets.UTF_8));

        return ImmutableCredentialRecord.builder()
                .credentialId(Bytes.fromBase64(passkey.getCredentialId()))
                .userEntityUserId(userIdInBytes)
                .publicKey(publicKeyObj)
                .signatureCount(passkey.getSignatureCount())
                .label(passkey.getLabel())
                .attestationObject(passkey.getAttestationObject() != null ? Bytes.fromBase64(passkey.getAttestationObject()) : null)
                .attestationClientDataJSON(passkey.getAttestationClientDataJSON() != null ? Bytes.fromBase64(passkey.getAttestationClientDataJSON()) : null)
                .transports(Collections.emptySet())
                .build();
    }
}
