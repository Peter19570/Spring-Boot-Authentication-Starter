package com.example.authstarter.features.auth.dto.response;

import com.example.authstarter.features.user.dto.response.UserResponse;

public record AuthResponse(
        Boolean authenticated,
        TokenResponse token,
        UserResponse userInfo
) {}
