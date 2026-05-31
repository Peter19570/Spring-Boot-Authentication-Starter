package com.example.authstarter.module.users.controller;

import com.example.authstarter.module.auth.dto.request.AccountDeletionRequest;
import com.example.authstarter.module.shared.dto.ApiResponse;
import com.example.authstarter.module.shared.dto.CustomUserPrincipal;
import com.example.authstarter.module.users.dto.response.UserDetailsResponse;
import com.example.authstarter.module.users.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> userInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal){
        UserDetailsResponse response = userService.userInfo(principal.user());
        return ResponseEntity.ok(new ApiResponse<>("User Information", response));
    }

    @PostMapping("/me/delete-request")
    public ResponseEntity<Void> requestDelete(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.initiateDeletion(principal.user());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> confirmDelete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody AccountDeletionRequest request) {
        userService.confirmSoftDelete(
                principal.user(),
                request.password(),
                request.otp()
        );
        return ResponseEntity.noContent().build();
    }
}
