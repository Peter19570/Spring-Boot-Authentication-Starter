package com.example.authstarter.features.auth.controller;

import com.example.authstarter.features.auth.dto.request.*;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.response.PasskeyOptionsResponse;
import com.example.authstarter.features.auth.dto.response.PasskeyResponse;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.auth.service.AuthService;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.webauthn.api.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Handles user authentication, registration, and account security operations.")
public class AuthController {

    private final AuthService authService;

    /**
     * MAJOR AUTHENTICATION APIS HERE
     */

    @PostMapping("/register")
    @Operation(summary = "Register a new user account.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
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
            @Valid @RequestBody GoogleRequest request
    ) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Google login success", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out by revoking a refresh token.")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
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
            @RequestParam
            @NotBlank(message = "Token is required")
            @Size(min = 43, max = 43, message = "Token must be exactly 43 characters")
            String token
    ) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification-email")
    @Operation(summary = "Resend email verification token.")
    public ResponseEntity<Void> resendVerificationEmail(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        authService.resendVerificationEmail(principal.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-email")
    @Operation(summary = "Request verification token to change email")
    public ResponseEntity<Void> requestChange(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody EmailChangeRequest request
    ) {
        authService.requestEmailChange(principal.id(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/confirm-email")
    @Operation(summary = "Verify and change email")
    public ResponseEntity<Void> confirmChange(
            @RequestParam
            @NotBlank(message = "Token is required")
            @Size(min = 43, max = 43, message = "Token must be exactly 43 characters")
            String token
    ) {
        authService.confirmEmailChange(token);
        return ResponseEntity.noContent().build();
    }

    /**
     * PASSKEY RELATED APIS HERE
     */

    @PostMapping("/passkeys/initialize")
    @Operation(summary = "Initialize passkey registration.")
    public ResponseEntity<ApiResponse<PasskeyOptionsResponse>> startPasskeyRegistration(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PasskeyOptionsResponse response = authService.startPasskeyRegistration(principal.id());
        return ResponseEntity.ok(ApiResponse.success("Passkey Registration Initiated", response));
    }

    @PostMapping("/passkeys/register")
    @Operation(summary = "Complete passkey registration.")
    public ResponseEntity<ApiResponse<PasskeyResponse>> finishPasskeyRegistration(
            @Valid @RequestBody PasskeyRegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PasskeyResponse response = authService.finishPasskeyRegistration(request, principal.id());
        return ResponseEntity.ok(ApiResponse.success("Public Key Saved", response));
    }

    @PostMapping("/passkeys/challenge")
    @Operation(summary = "Generate passkey authentication challenge.")
    public ResponseEntity<ApiResponse<PublicKeyCredentialRequestOptions>> startPasskeyAuthentication() {
        PublicKeyCredentialRequestOptions response = authService.startPasskeyAuthentication();
        return ResponseEntity.ok(ApiResponse.success("Passkey Challenge Sent Successfully", response));
    }

    @PostMapping("/passkeys/login")
    @Operation(summary = "Complete passkey authentication challenge.")
    public ResponseEntity<ApiResponse<AuthResponse>> finishPasskeyAuthentication(
            @Valid @RequestBody PasskeyLoginRequest request
    ) {
        AuthResponse response = authService.finishPasskeyAuthentication(request);
        return ResponseEntity.ok(ApiResponse.success("Passkey Login Success", response));
    }

    @GetMapping("/passkeys")
    @Operation(summary = "Shows list of passkeys user has created")
    public ResponseEntity<ApiResponse<List<PasskeyResponse>>> getAllPasskeys(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<PasskeyResponse> responses = authService.findAllUserPasskeys(principal.id());
        return ResponseEntity.ok(ApiResponse.success("User Passkey List", responses));
    }

    @DeleteMapping("/passkeys/{passkeyId}")
    @Operation(summary = "Remove a registered passkey.")
    public ResponseEntity<Void> deleteSavedPasskey(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID passkeyId
    ) {
        authService.deleteSavedPasskey(principal.id(), passkeyId);
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
