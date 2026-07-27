package com.example.authstarter.features.auth.config.passkey;

import com.example.authstarter.features.user.model.User;
import com.example.authstarter.features.user.repo.UserRepo;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasskeyUserEntityConfig implements PublicKeyCredentialUserEntityRepository {

    private final UserRepo userRepo;

    @Override
    public @Nullable PublicKeyCredentialUserEntity findById(Bytes id) {
        UUID userId = UUID.fromString(new String(id.getBytes(), StandardCharsets.UTF_8));
        return userRepo.findById(userId)
                .map(this::toUserEntity)
                .orElse(null);
    }

    @Override
    public @Nullable PublicKeyCredentialUserEntity findByUsername(String email) {
        return userRepo.findByEmail(email)
                .map(this::toUserEntity)
                .orElse(null);
    }

    @Override
    public void save(PublicKeyCredentialUserEntity userEntity) {
    }

    @Override
    public void delete(Bytes id) {
    }

    private PublicKeyCredentialUserEntity toUserEntity(User user) {
        Bytes userIdBytes = new Bytes(user.getId().toString().getBytes(StandardCharsets.UTF_8));
        return ImmutablePublicKeyCredentialUserEntity.builder()
                .id(userIdBytes)
                .name(user.getEmail())
                .displayName(user.getFirstName() + " " + user.getLastName())
                .build();
    }
}
