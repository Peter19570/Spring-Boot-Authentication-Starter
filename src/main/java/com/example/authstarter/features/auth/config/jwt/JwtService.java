package com.example.authstarter.features.auth.config.jwt;

import com.example.authstarter.features.auth.dto.internal.JwtClaims;
import com.example.authstarter.features.shared.dto.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.authstarter.features.auth.constants.JwtConstants.*;

@Component
public class JwtService {

    @Value("${jwt.token.secret.key}")
    private String secretKey;

    @Getter
    @Value("${jwt.token.access.token.expiration:PT15M}")
    private Duration accessTokenExpiration;

    @Getter
    @Value("${jwt.token.refresh.token.expiration:P7D}")
    private Duration refreshTokenExpiration;

    public String generateAccessToken(UserPrincipal principal) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        claims.put(EMAIL, principal.email());
        claims.put(GRANTED_AUTHORITIES, roles);
        claims.put(TOKEN_TYPE, ACCESS_VALUE);

        return createToken(claims, principal.id().toString(), accessTokenExpiration.toMillis());
    }

    public String generateRefreshToken(UserPrincipal principal) {
        Map<String, Object> claims = new HashMap<>();

        claims.put(TOKEN_TYPE, REFRESH_VALUE);
        return createToken(claims, principal.id().toString(), refreshTokenExpiration.toMillis());
    }

    public boolean isTokenValid(boolean expiredToken) {
        return !expiredToken;
    }

    public JwtClaims extractToken(String token){
        Claims claims = extractAllClaims(token);
        return JwtClaims.extracted(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMillis) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}