package com.example.authstarter.features.auth.model;

import com.example.authstarter.features.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "passkeys")
public class Passkey extends BaseEntity {

    @Column(nullable = false, updatable = false, unique = true)
    private String credentialId;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(columnDefinition = "TEXT")
    private String attestationObject;

    @Column(columnDefinition = "TEXT")
    private String attestationClientDataJSON;

    @Column(nullable = false)
    private long signatureCount;

    private String label;
    private Instant lastUsed;

}
