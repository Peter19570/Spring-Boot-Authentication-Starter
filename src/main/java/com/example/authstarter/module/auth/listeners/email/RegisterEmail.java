package com.example.authstarter.module.auth.listeners.email;

import com.example.authstarter.module.auth.model.EmailVerificationToken;
import com.example.authstarter.module.auth.repo.EmailVerificationTokenRepo;
import com.example.authstarter.module.auth.service.notification.EmailService;
import com.example.authstarter.module.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RegisterEmail {

    private final EmailService emailService;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;

    @EventListener
    public void onUserRegistration(User user){
        String rawToken = UUID.randomUUID().toString();
        emailService.sendVerificationEmail(user, rawToken);

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setTokenHash(hashToken(rawToken));
        verificationToken.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));
        emailVerificationTokenRepo.save(verificationToken);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
