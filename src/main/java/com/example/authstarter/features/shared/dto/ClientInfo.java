package com.example.authstarter.features.auth.dto.internal;

import jakarta.servlet.http.HttpServletRequest;

public record ClientInfo(
        String ipAddress,
        String userAgent
) {
    public static ClientInfo save(HttpServletRequest servletRequest){
        String agent = servletRequest.getHeader("User-")
        return new ClientInfo()
    }
}
