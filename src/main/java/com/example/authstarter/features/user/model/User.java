package com.example.authstarter.features.user.model;

import com.example.authstarter.features.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Arrays;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    private String password;
    private String firstName;
    private String lastName;
    private String picture;
    private Instant lockedUntil;
    private Instant deletedAt;
    private String provider;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean isLocked = false;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    // Add roles here

    public static final String NOT_SET = "not-set";

    /** The provider column holds a comma-separated list (e.g. "GOOGLE,PASSKEY"). */
    public boolean hasProvider(String name) {
        return provider == null || !Arrays.asList(provider.split(",")).contains(name);
    }

    /** Full name, or null when no real name is set. */
    public String displayName() {
        boolean noFirst = isUnset(firstName);
        boolean noLast = isUnset(lastName);

        if (noFirst && noLast) return null;
        if (noLast) return firstName.trim();
        if (noFirst) return lastName.trim();
        return firstName.trim() + " " + lastName.trim();
    }

    private static boolean isUnset(String value) {
        return value == null || value.isBlank() || NOT_SET.equals(value);
    }

}
