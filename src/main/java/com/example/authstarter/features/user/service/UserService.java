package com.example.authstarter.features.user.service;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.auth.exceptions.AuthenticationException;
import com.example.authstarter.features.auth.exceptions.NotFoundException;
import com.example.authstarter.features.auth.exceptions.ValidationException;
import com.example.authstarter.features.auth.repo.EmailVerificationTokenRepo;
import com.example.authstarter.features.auth.repo.PasskeyRepo;
import com.example.authstarter.features.auth.repo.PasswordResetTokenRepo;
import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import com.example.authstarter.features.auth.service.helpers.AuthHelper;
import com.example.authstarter.features.auth.service.notification.EmailService;
import com.example.authstarter.features.auth.service.notification.OtpService;
import com.example.authstarter.features.user.dto.response.UserDetailsResponse;
import com.example.authstarter.features.user.mapper.UserMapper;
import com.example.authstarter.features.user.model.User;
import com.example.authstarter.features.user.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
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
    private final OtpService otpService;
    private final PasskeyRepo passkeyRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepo refreshTokenRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "all-users", key = "#id")
    public User fetchUser(UUID id){
        return userRepo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse getCurrentUser(UUID currentUserId){
        User currentUser = fetchUser(currentUserId);
        return userMapper.toDetailsDto(currentUser);
    }

    public void initiateDeletion(UUID currentUserId) {
        User currentUser = fetchUser(currentUserId);
        String code = otpService.generateOtp(currentUser.getEmail());
        emailService.sendAccountDeletionCode(currentUser, code);
    }

    public void confirmSoftDelete(UUID currentUserId, String password, String otp) {
        User currentUser = fetchUser(currentUserId);

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
        passkeyRepo.deleteAllByUserId(currentUser.getId());

        eventPublisher.publishEvent(new AuditRequest(currentUser, AuditAction.ACCOUNT_SOFT_DELETED,
                Map.of("message", "User has been soft deleted")));
    }
}
