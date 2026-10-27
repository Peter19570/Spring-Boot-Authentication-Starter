package com.example.authstarter.features.auth.model;

import com.example.authstarter.features.shared.model.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "passkeys", indexes = @Index(name = "idx_passkeys_user_id", columnList = "user_id"))
public class Passkey extends BaseEntity {

    @Column(nullable = false, updatable = false, unique = true)
    private String credentialId;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(columnDefinition = "TEXT")
    private String attestationObject;

    @Column(columnDefinition = "TEXT")
    private String attestationClientDataJSON;

    @Column(nullable = false)
    private long signatureCount;

    // WebAuthn flags: these must round-trip, or synced passkeys can fail verification
    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean uvInitialized;

    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean backupEligible;

    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean backupState;

    private String label;
    private Instant lastUsed;

}
