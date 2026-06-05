package com.example.authstarter.features.auth.controller;

import com.example.authstarter.features.auth.dto.request.*;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.auth.service.AuthService;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

//    =========================================================================================
//    MAJOR AUTHENTICATION METHODS HERE
//    =========================================================================================

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody AuthRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                "Register success", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(
                "Login success", response));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> google(
            @RequestBody @Valid GoogleRequest request)
            throws GeneralSecurityException, IOException {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(new ApiResponse<>(
                "Google login success", response));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        authService.logout(request, principal.user());
        return ResponseEntity.ok(new ApiResponse<>(
                "Logout success", "You have successfully logged out of your account."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(new ApiResponse<>(
                "Token refresh success", response));
    }

//    =========================================================================================
//    EMAIL RELATED METHODS HERE
//    =========================================================================================

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam @NotNull(message = "Token is required") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(new ApiResponse<>(
                "Verification Complete", "Identity verification successful."));
    }

    @PostMapping("/change-email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> requestChange(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid EmailChangeRequest request) {

        authService.requestEmailChange(principal.user(), request);
        return ResponseEntity.ok(new ApiResponse<>(
                "Verification Required", "Please check your new inbox and click" +
                " the secure activation link we just sent you."));
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<ApiResponse<String>> confirmChange(
            @RequestParam("token") @NotNull(message = "Token is required") String token) {
        authService.confirmEmailChange(token);
        return ResponseEntity.ok(new ApiResponse<>(
                "Email Address Updated", "Your primary email " +
                "address has been successfully changed."));
    }

//    =========================================================================================
//    PASSWORD RELATED METHODS HERE
//    =========================================================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(new ApiResponse<>(
                "Password Reset Initiated",
                "An email containing further instructions has been dispatched to " +
                        "the provided address, provided a corresponding account exists."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new ApiResponse<>(
                "Password Updated", "Your password has been successfully reset." +
                " You may now log in with your new credentials."));
    }
}
