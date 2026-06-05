package com.example.authstarter.features.auth.model;

import com.example.authstarter.features.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "email_verification_tokens")
@EntityListeners(AuditingEntityListener.class)
public class EmailVerificationToken{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column
    private String newEmail;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private Instant expiresAt;

    @CreatedDate
    private Instant createdAt;
}
