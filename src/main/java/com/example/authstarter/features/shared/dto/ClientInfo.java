package com.example.authstarter.features.shared.dto;

import jakarta.servlet.http.HttpServletRequest;

import static com.example.authstarter.features.shared.constants.ClientConstants.IP_ADDRESS;
import static com.example.authstarter.features.shared.constants.ClientConstants.USER_AGENT;

public record ClientInfo( // Adjust to take client info from request
        String ipAddress,
        String userAgent
) {
    public static ClientInfo save(HttpServletRequest servletRequest){
        String agent = servletRequest.getHeader(USER_AGENT);

        String ip = servletRequest.getHeader(IP_ADDRESS);
        if (ip == null || ip.isBlank()) {
            ip = servletRequest.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        return new ClientInfo(ip, agent);
    }
}
