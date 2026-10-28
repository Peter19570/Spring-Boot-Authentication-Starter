package com.example.authstarter.features.shared.utils;

import com.example.authstarter.features.shared.dto.ClientInfo;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ClientInfoUtils {

    private ClientInfoUtils() {}

    public static ClientInfo getClientInfo(){
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return ClientInfo.save(attributes.getRequest());
        }

        // No HTTP request bound to this thread (async work, scheduled job): don't crash, just log no client
        return new ClientInfo(null, null);
    }
}