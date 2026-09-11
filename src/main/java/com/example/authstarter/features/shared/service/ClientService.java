package com.example.authstarter.features.shared.service;

import com.example.authstarter.features.shared.dto.ClientInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ClientInfoService {

    public static ClientInfo getClientInfo(){
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        return ClientInfo.save(req);
    }
}
