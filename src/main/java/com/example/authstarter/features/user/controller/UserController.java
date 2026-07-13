package com.example.authstarter.features.user.controller;

import com.example.authstarter.features.auth.dto.request.AccountDeletionRequest;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import com.example.authstarter.features.user.dto.response.UserDetailsResponse;
import com.example.authstarter.features.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal principal){
        UserDetailsResponse response = userService.getCurrentUser(principal.id());
        return ResponseEntity.ok(ApiResponse.success("Current User Information", response));
    }

    @PostMapping("/me/deletion-request")
    public ResponseEntity<Void> requestDelete(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.initiateDeletion(principal.id());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> confirmDelete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody AccountDeletionRequest request) {
        userService.confirmSoftDelete(
                principal.id(),
                request.password(),
                request.otp()
        );
        return ResponseEntity.noContent().build();
    }
}
