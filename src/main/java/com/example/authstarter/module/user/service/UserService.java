package com.example.authstarter.module.user.service;

import com.example.authstarter.module.audit.dto.AuditRequest;
import com.example.authstarter.module.audit.enums.AuditAction;
import com.example.authstarter.module.auth.exceptions.ValidationException;
import com.example.authstarter.module.auth.repo.EmailVerificationTokenRepo;
import com.example.authstarter.module.auth.repo.PasswordResetTokenRepo;
import com.example.authstarter.module.auth.repo.RefreshTokenRepo;
import com.example.authstarter.module.auth.service.notification.EmailService;
import com.example.authstarter.module.auth.service.notification.OtpService;
import com.example.authstarter.module.user.dto.response.UserDetailsResponse;
import com.example.authstarter.module.user.mapper.UserMapper;
import com.example.authstarter.module.user.model.User;
import com.example.authstarter.module.user.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepo refreshTokenRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final ApplicationEventPublisher eventPublisher;

    public User syncUser(GoogleIdToken.Payload payload){
        User existingUser =  userRepo.findByEmail(payload.getEmail()).orElseGet(() -> {
            User user = userMapper.toEntityFromGoogle(payload);

            eventPublisher.publishEvent(new AuditRequest(user, AuditAction.REGISTER,
                    Map.of("message", "User created account with Google login")));

            return userRepo.save(user);
        });

        if (existingUser.getFirstName() == null){
            existingUser.setFirstName(payload.get("given_name").toString());
            existingUser.setLastName(payload.get("family_name").toString());
            existingUser.setPicture(payload.get("picture").toString());
            existingUser.setEmailVerified(true);

            String provider = existingUser.getProvider();

            if (provider == null) {
                existingUser.setProvider("GOOGLE");
            } else if (!provider.contains("GOOGLE")) {
                existingUser.setProvider(provider + ",GOOGLE");
            }

            userRepo.save(existingUser);
        }

        eventPublisher.publishEvent(new AuditRequest(existingUser, AuditAction.LOGIN,
                Map.of("message", "Google login success")));

        return existingUser;
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse getCurrentUser(User currentUser){
        return userMapper.toDetailsDto(currentUser);
    }

    public void initiateDeletion(User currentUser) {
        String code = otpService.generateOtp(currentUser.getEmail());
        emailService.sendAccountDeletionCode(currentUser, code);
    }

    public void confirmSoftDelete(User currentUser, String password, String otp) {
        if (currentUser.getPassword() != null) {
            if (!passwordEncoder.matches(password, currentUser.getPassword())) {
                throw new BadCredentialsException("Invalid password provided for account deletion.");
            }
        }

        if (!otpService.validateOtp(currentUser.getEmail(), otp.replaceAll("\\s+", ""))) {
            throw new ValidationException("Invalid or expired deletion code.");
        }

        currentUser.setDeletedAt(Instant.now());
        refreshTokenRepo.deleteByUserId(currentUser.getId());
        passwordResetTokenRepo.deleteByUserId(currentUser.getId());
        emailVerificationTokenRepo.deleteByUserId(currentUser.getId());
        userRepo.save(currentUser);

        eventPublisher.publishEvent(new AuditRequest(currentUser, AuditAction.ACCOUNT_SOFT_DELETED,
                Map.of("message", "User has been soft deleted")));
    }
}
