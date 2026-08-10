package com.example.authstarter.features.auth.listeners.email;

import com.example.authstarter.features.auth.model.EmailVerificationToken;
import com.example.authstarter.features.auth.repo.EmailVerificationTokenRepo;
import com.example.authstarter.features.auth.service.notification.EmailService;
import com.example.authstarter.features.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.example.authstarter.features.auth.service.helpers.AuthHelper.hashToken;

@Component
@RequiredArgsConstructor
public class VerificationEmail {

    private final EmailService emailService;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;

    @EventListener
    public void onSendVerificationEmail(User user){
        String rawToken = UUID.randomUUID().toString();
        emailService.sendVerificationEmail(user, rawToken);

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setTokenHash(hashToken(rawToken));
        verificationToken.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));
        emailVerificationTokenRepo.save(verificationToken);
    }
}
