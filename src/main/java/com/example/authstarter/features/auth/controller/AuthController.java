package com.example.authstarter.features.auth.controller;

import com.example.authstarter.features.auth.dto.request.*;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.response.PasskeyOptionsResponse;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.auth.service.AuthService;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.webauthn.api.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Handles user authentication, registration, and account security operations.")
public class AuthController {

    private final AuthService authService;

    /**
     * MAJOR AUTHENTICATION APIS HERE
     */

    @PostMapping("/register")
    @Operation(summary = "Register a new user account.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody AuthRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Register success", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login success", response));
    }

    @PostMapping("/google")
    @Operation(summary = "Authenticate with Google.")
    public ResponseEntity<ApiResponse<AuthResponse>> google(
            @Valid @RequestBody GoogleRequest request)
            throws GeneralSecurityException, IOException {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Google login success", response));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Log out authenticated user.")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        authService.logout(request, principal.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token.")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token refresh success", response));
    }

    /**
     * EMAIL RELATED APIS HERE
     */

    @GetMapping("/verify-email")
    @Operation(summary = "Verify user's email address.")
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody VerificationTokenRequest request
    ) {
        authService.verifyEmail(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resend-verification-email")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resend email verification token.")
    public ResponseEntity<Void> resendVerificationEmail(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        authService.resendVerificationEmail(principal.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-email")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Request verification token to change email")
    public ResponseEntity<Void> requestChange(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody EmailChangeRequest request
    ) {
        authService.requestEmailChange(principal.id(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/confirm-email")
    @Operation(summary = "Verify and change email")
    public ResponseEntity<Void> confirmChange(
            @Valid @RequestBody VerificationTokenRequest request
    ) {
        authService.confirmEmailChange(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * PASSKEY RELATED APIS HERE
     */

    @PostMapping("/passkeys/initialize")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initialize passkey registration.")
    public ResponseEntity<ApiResponse<PasskeyOptionsResponse>> startPasskeyRegistration(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse
    ) {
        PasskeyOptionsResponse response = authService.startPasskeyRegistration(
                servletRequest, servletResponse);
        return ResponseEntity.ok(ApiResponse.success("Passkey Registration Initiated", response));
    }

    @PostMapping("/passkeys/register")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete passkey registration.")
    public ResponseEntity<ApiResponse<CredentialRecord>> finishPasskeyRegistration(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse,
            @RequestBody PasskeyRegistrationRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CredentialRecord response = authService.finishPasskeyRegistration(
                servletRequest, servletResponse, request, principal.id());
        return ResponseEntity.ok(ApiResponse.success("Public Key Saved", response));
    }

    @PostMapping("/passkeys/challenge")
    @Operation(summary = "Generate passkey authentication challenge.")
    public ResponseEntity<ApiResponse<PublicKeyCredentialRequestOptions>> startPasskeyAuthentication(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse
    ) {
        PublicKeyCredentialRequestOptions response = authService.startPasskeyAuthentication(
                servletRequest, servletResponse);
        return ResponseEntity.ok(ApiResponse.success("Passkey Challenge Sent Successfully", response));
    }

    @PostMapping("/passkeys/login")
    @Operation(summary = "Verify a passkey authentication challenge.")
    public ResponseEntity<ApiResponse<AuthResponse>> finishPasskeyAuthentication(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse,
            @RequestBody PasskeyLoginRequest request
    ) {
        AuthResponse response = authService.finishPasskeyAuthentication(
                servletRequest, servletResponse, request);
        return ResponseEntity.ok(ApiResponse.success("Passkey Login Success", response));
    }

    @DeleteMapping("/passkeys/{credentialId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove a registered passkey.")
    public ResponseEntity<Void> deleteSavedPasskey(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String credentialId
    ) {
        authService.deleteSavedPasskey(principal.id(), credentialId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PASSWORD RELATED APIS HERE
     */

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset.")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.requestPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user's password using their password and otp.")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
