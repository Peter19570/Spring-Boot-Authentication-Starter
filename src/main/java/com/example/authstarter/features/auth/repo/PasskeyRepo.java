package com.example.authstarter.features.auth.repo;

import com.example.authstarter.features.auth.model.Passkey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasskeyRepo extends JpaRepository<Passkey, UUID> {

    Optional<Passkey> findByCredentialId(String credentialId);

    List<Passkey> findAllByUserId(UUID userId);

    void deleteByCredentialId(String credentialId);

    void deleteByIdAndUserId(UUID passkeyId, UUID userId);

    void deleteAllByUserId(UUID userId);
}
