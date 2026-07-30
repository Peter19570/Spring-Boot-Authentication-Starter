package com.example.authstarter.features.user.controller;

import com.example.authstarter.features.auth.dto.request.AccountDeletionRequest;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import com.example.authstarter.features.user.dto.response.UserDetailsResponse;
import com.example.authstarter.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Basic user(me) endpoints made available")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Retrieve the authenticated user's profile.")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserDetailsResponse response = userService.getCurrentUser(principal.id());
        return ResponseEntity.ok(ApiResponse.success("Current User Information", response));
    }

    @PostMapping("/me/deletion-request")
    @Operation(summary = "Request account deletion.")
    public ResponseEntity<ApiResponse<String>> requestDelete(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        userService.initiateDeletion(principal.id());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Deletion Request Initiated",
                        "Account deletion request submitted successfully"));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete the authenticated user's account.")
    public ResponseEntity<ApiResponse<String>> confirmDelete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody AccountDeletionRequest request
    ) {
        userService.confirmSoftDelete(
                principal.id(),
                request.password(),
                request.otp()
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(
                        "Account Deleted",
                        "User account has been deleted successfully"));
    }
}
