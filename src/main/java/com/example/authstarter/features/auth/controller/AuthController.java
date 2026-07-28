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
import jakarta.validation.constraints.NotNull;
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
    @Operation(summary = "Authenticate a user.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login success", response));
    }

    @PostMapping("/google")
    @Operation(summary = "Authenticate with Google.")
    public ResponseEntity<ApiResponse<AuthResponse>> google(
            @RequestBody @Valid GoogleRequest request)
            throws GeneralSecurityException, IOException {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Google login success", response));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Log out the authenticated user.")
    public ResponseEntity<ApiResponse<String>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        authService.logout(request, principal.id());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logout success",
                        "You have successfully logged out of your account."));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh an expired access token.")
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
    @Operation(summary = "Verify a user's email address.")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam
            @NotNull(message = "Email verification token is required") String token
    ) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(
                "Verification Complete",
                "Identity verification successful."));
    }

    @GetMapping("/resend-verification-email")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resend email verification.")
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ){
        authService.resendVerificationEmail(principal);
        return ResponseEntity.ok(ApiResponse.success(
                "Verification Email Resent",
                "Kindly check your inbox for the new verification link that has been sent"));
    }

    @PostMapping("/change-email")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change the user's email address.")
    public ResponseEntity<ApiResponse<String>> requestChange(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid EmailChangeRequest request
    ) {
        authService.requestEmailChange(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(
                "Verification Required",
                "Please check your new inbox and click the secure activation link we just sent you."));
    }

    @GetMapping("/confirm-email")
    @Operation(summary = "Confirm a user's email address.")
    public ResponseEntity<ApiResponse<String>> confirmChange(
            @RequestParam
            @NotNull(message = "Email verification token is required") String token
    ) {
        authService.confirmEmailChange(token);
        return ResponseEntity.ok(ApiResponse.success(
                "Email Address Updated",
                "Your primary email address has been successfully changed."));
    }

    /**
     * PASSKEY RELATED APIS HERE
     */

    @PostMapping("/passkeys/initialize")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initialize passkey registration.")
    public ResponseEntity<ApiResponse<PasskeyOptionsResponse>> startPasskeyRegistration(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ){
        PasskeyOptionsResponse response = authService.startPasskeyRegistration(
                servletRequest, servletResponse);
        return ResponseEntity.ok(ApiResponse.success("Passkey Registration Initiated", response));
    }

    @PostMapping("/passkeys/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete passkey registration.")
    public ResponseEntity<ApiResponse<CredentialRecord>> finishPasskeyRegistration(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse,
            @RequestBody PasskeyRegistrationRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ){
        CredentialRecord response = authService.finishPasskeyRegistration(
                servletRequest, servletResponse, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Public Key Saved", response));
    }

    @PostMapping("/passkeys/challenge")
    @Operation(summary = "Generate a passkey authentication challenge.")
    public ResponseEntity<ApiResponse<PublicKeyCredentialRequestOptions>> startPasskeyAuthentication(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ){
        PublicKeyCredentialRequestOptions response = authService.startPasskeyAuthentication(
                servletRequest, servletResponse);
        return ResponseEntity.ok(ApiResponse.success("Passkey Challenge Sent Successfully", response));
    }

    @PostMapping("/passkeys/verify")
    @Operation(summary = "Verify a passkey authentication challenge.")
    public ResponseEntity<ApiResponse<AuthResponse>> finishPasskeyAuthentication(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse,
            @RequestBody PasskeyLoginRequest request
    ){
        AuthResponse response = authService.finishPasskeyAuthentication(
                servletRequest, servletResponse, request);
        return ResponseEntity.ok(ApiResponse.success("Passkey Login Success", response));
    }

    @DeleteMapping("/passkeys/{credentialId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove a registered passkey.")
    public ResponseEntity<ApiResponse<String>> deleteSavedPasskey(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String credentialId
    ){
        authService.deleteSavedPasskey(principal, credentialId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(
                        "Passkey Deletion Triggered",
                        "The selected passkey will be removed."));
    }

    /**
     * PASSWORD RELATED APIS HERE
     */

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset.")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Password Reset Initiated",
                "An email containing further instructions has been dispatched to " +
                        "the provided address, provided a corresponding account exists."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset a user's password using a valid password reset token.")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success(
                "Password Updated",
                "Your password has been successfully reset." +
                " You may now log in with your new credentials."));
    }
}
