package com.example.authstarter.features.user.service;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.auth.dto.request.AccountDeletionRequest;
import com.example.authstarter.features.auth.exceptions.ValidationException;
import com.example.authstarter.features.auth.repo.EmailVerificationTokenRepo;
import com.example.authstarter.features.auth.repo.PasskeyRepo;
import com.example.authstarter.features.auth.repo.PasswordResetTokenRepo;
import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import com.example.authstarter.features.auth.service.helpers.AuthHelper;
import com.example.authstarter.features.auth.service.notification.EmailService;
import com.example.authstarter.features.auth.service.notification.OTPService;
import com.example.authstarter.features.user.dto.response.UserDetailedResponse;
import com.example.authstarter.features.user.mapper.UserMapper;
import com.example.authstarter.features.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
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

    private final AuthHelper authHelper;
    private final UserMapper userMapper;
    private final OTPService otpService;
    private final PasskeyRepo passkeyRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepo refreshTokenRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;

    @Transactional(readOnly = true)
    public UserDetailedResponse getCurrentUser(UUID userId){
        User currentUser = authHelper.fetchUser(userId);
        return userMapper.toDetailedDto(currentUser);
    }

    public void initiateDeletion(UUID userId) {
        User currentUser = authHelper.fetchUser(userId);
        String code = otpService.generateOtp(currentUser.getEmail());
        emailService.sendAccountDeletionCode(currentUser, code);
    }

    @CacheEvict(cacheNames = {"users", "all-users"}, key = "#userId")
    public void confirmSoftDelete(UUID userId, AccountDeletionRequest request) {
        User currentUser = authHelper.fetchUserFresh(userId);

        if (currentUser.getPassword() != null) {
            if (!passwordEncoder.matches(request.password(), currentUser.getPassword())) {
                throw new BadCredentialsException("Invalid password provided for account deletion.");
            }
        }

        if (!otpService.validateOtp(
                currentUser.getEmail(),
                request.otp().replaceAll("\\s+", ""))
        ) {
            throw new ValidationException("Invalid or expired deletion code.");
        }

        currentUser.setDeletedAt(Instant.now());
        refreshTokenRepo.deleteAllByUserId(currentUser.getId());
        passwordResetTokenRepo.deleteAllByUserId(currentUser.getId());
        emailVerificationTokenRepo.deleteAllByUserId(currentUser.getId());
        passkeyRepo.deleteAllByUserId(currentUser.getId());

        eventPublisher.publishEvent(AuditRequest.log(currentUser, AuditAction.ACCOUNT_SOFT_DELETED,
                "User account has been soft deleted", Map.of()));
    }
}
