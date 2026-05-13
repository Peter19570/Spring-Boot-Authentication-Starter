package com.example.authstarter.module.auth.dto.response;

import com.example.authstarter.module.users.dto.response.UserResponse;

public record AuthResponse(
        Boolean authenticated,
        TokenResponse token,
        UserResponse userInfo
) {}
