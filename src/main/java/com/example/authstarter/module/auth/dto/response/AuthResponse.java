package com.example.authstarter.module.auth.dto.response;

import com.example.authstarter.module.user.dto.response.UserResponse;

public record AuthResponse(
        Boolean authenticated,
        TokenResponse token,
        UserResponse userInfo
) {}
