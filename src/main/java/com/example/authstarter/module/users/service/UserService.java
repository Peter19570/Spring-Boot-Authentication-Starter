package com.example.authstarter.module.users.service;

import com.example.authstarter.module.audit.dto.AuditRequest;
import com.example.authstarter.module.audit.enums.AuditAction;
import com.example.authstarter.module.audit.service.AuditService;
import com.example.authstarter.module.auth.exceptions.NotFoundException;
import com.example.authstarter.module.auth.exceptions.ValidationException;
import com.example.authstarter.module.auth.repo.EmailVerificationTokenRepo;
import com.example.authstarter.module.auth.repo.PasswordResetTokenRepo;
import com.example.authstarter.module.auth.repo.RefreshTokenRepo;
import com.example.authstarter.module.auth.service.notification.EmailService;
import com.example.authstarter.module.auth.service.notification.OtpService;
import com.example.authstarter.module.users.dto.response.UserDetailsResponse;
import com.example.authstarter.module.users.mapper.UserMapper;
import com.example.authstarter.module.users.model.User;
import com.example.authstarter.module.users.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepo refreshTokenRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;

    public User syncUser(GoogleIdToken.Payload payload){
        User existingUser =  userRepo.findByEmail(payload.getEmail()).orElseGet(() -> {
            User user = userMapper.toEntityFromGoogle(payload);

            auditService.handleAuditEvent(new AuditRequest(user, AuditAction.REGISTER,
                    Map.of("message", "User created account with Google login")));

            return userRepo.save(user);
        });

        if (existingUser.getFirstName() == null){
            existingUser.setFirstName(payload.get("given_name").toString());
            existingUser.setLastName(payload.get("family_name").toString());
            existingUser.setPicture(payload.get("picture").toString());
            existingUser.setEmailVerified(true);
            existingUser.setProvider("GOOGLE");
            userRepo.save(existingUser);
        }

        auditService.handleAuditEvent(new AuditRequest(existingUser, AuditAction.LOGIN,
                Map.of("message", "Google login success")));

        return existingUser;
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse userInfo(User user){
        return userMapper.toDetailsDto(user);
    }

    public void initiateDeletion(User user) {
        String code = otpService.generateOtp(user.getEmail());
        emailService.sendAccountDeletionCode(user.getEmail(), code);
    }

    public void confirmSoftDelete(UUID userId, String password, String otp) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getPassword() != null) {
            if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Invalid password provided for account deletion.");
            }
        }

        if (!otpService.validateOtp(user.getEmail(), otp.replaceAll("\\s+", ""))) {
            throw new ValidationException("Invalid or expired deletion code.");
        }

        user.setDeletedAt(Instant.now());
        refreshTokenRepo.deleteByUser(user);
        passwordResetTokenRepo.deleteByUser(user);
        emailVerificationTokenRepo.deleteByUser(user);
        userRepo.save(user);

        auditService.handleAuditEvent(new AuditRequest(user, AuditAction.ACCOUNT_SOFT_DELETED,
                Map.of("message", "User has been soft deleted")));
    }
}
