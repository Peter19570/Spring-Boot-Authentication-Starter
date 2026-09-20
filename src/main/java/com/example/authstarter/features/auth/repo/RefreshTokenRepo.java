package com.example.authstarter.features.auth.repo;

import com.example.authstarter.features.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteAllByUserId(UUID userId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update RefreshToken rt
       set rt.revoked = true, rt.revokedAt = :now
     where rt.tokenHash = :hash
       and rt.revoked = false
       and rt.expiresAt > :now
    """)
    int revokeIfActive(@Param("hash") String hash, @Param("now") Instant now);

    boolean existsByTokenHashAndRevokedTrueAndRevokedAtAfterAndExpiresAtAfter(
            String tokenHash, Instant revokedAfter, Instant now);

}
