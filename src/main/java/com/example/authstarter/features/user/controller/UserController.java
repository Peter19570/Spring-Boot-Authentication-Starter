package com.example.authstarter.features.user.controller;

import com.example.authstarter.features.auth.dto.request.AccountDeletionRequest;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import com.example.authstarter.features.user.dto.response.UserDetailedResponse;
import com.example.authstarter.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Basic user(me) endpoints made available")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Retrieve the authenticated user's profile.")
    public ResponseEntity<ApiResponse<UserDetailedResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserDetailedResponse response = userService.getCurrentUser(principal.id());
        return ResponseEntity.ok(ApiResponse.success("Current User Information", response));
    }

    @PostMapping("/me/deletion-request")
    @Operation(summary = "Request account deletion.")
    public ResponseEntity<Void> requestDelete(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        userService.initiateDeletion(principal.id());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete authenticated user's account.")
    public ResponseEntity<Void> confirmDelete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AccountDeletionRequest request
    ) {
        userService.confirmSoftDelete(principal.id(), request);
        return ResponseEntity.noContent().build();
    }
}
