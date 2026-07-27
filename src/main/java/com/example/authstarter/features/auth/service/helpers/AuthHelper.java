package com.example.authstarter.features.auth.service.helpers;

import com.example.authstarter.features.auth.config.jwt.JwtService;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.auth.model.RefreshToken;
import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import com.example.authstarter.features.user.mapper.UserMapper;
import com.example.authstarter.features.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserMapper userMapper;
    private final RefreshTokenRepo refreshTokenRepo;

    public AuthResponse createAuthResponse(JwtService jwtService, User user){
        return new AuthResponse(
                true,
                createTokenResponse(jwtService, user),
                userMapper.toDto(user)
        );
    }

    public TokenResponse createTokenResponse(JwtService jwtService, User user){
        CustomUserPrincipal principal = new CustomUserPrincipal(user);

        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal);
        long accessExpiration = jwtService.getAccessExpirationInSeconds();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(refresh);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 7));
        refreshTokenRepo.save(refreshToken);

        return new TokenResponse(access, refresh, accessExpiration);
    }

    public static String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
