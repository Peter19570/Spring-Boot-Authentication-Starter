package com.example.authstarter.features.auth.dto.internal;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.example.authstarter.features.auth.constants.JWTConstants.*;

public record JwtResponse( // Adjust to add more extracted claims as needed
        UUID userId,
        String email,
        String tokenType,
        boolean expired,
        List<SimpleGrantedAuthority> authorities
) {
    public static JwtResponse extracted(Claims claims){
        return new JwtResponse(
                UUID.fromString(claims.getSubject()),
                claims.get(EMAIL, String.class),
                claims.get(TOKEN_TYPE, String.class),
                claims.getExpiration().before(new Date()),
                claims.get(GRANTED_AUTHORITIES, List.class)
        );
    }
}
