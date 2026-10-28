package com.example.authstarter.features.shared.dto;

import jakarta.servlet.http.HttpServletRequest;

import static com.example.authstarter.features.shared.constants.ClientConstants.USER_AGENT;

public record ClientInfo( // Adjust to take client info from request
        String ipAddress,
        String userAgent
) {
    private static final int MAX_USER_AGENT_LENGTH = 255; // matches the audit_logs.user_agent column

    public static ClientInfo save(HttpServletRequest servletRequest){
        String agent = servletRequest.getHeader(USER_AGENT);

        if (agent != null && agent.length() > MAX_USER_AGENT_LENGTH) {
            agent = agent.substring(0, MAX_USER_AGENT_LENGTH);
        }

        // getRemoteAddr() only: with server.forward-headers-strategy=native, Tomcat has already resolved the
        // real client IP from trusted proxies. Reading X-Forwarded-For here would let clients forge audit IPs.
        return new ClientInfo(servletRequest.getRemoteAddr(), agent);
    }
}