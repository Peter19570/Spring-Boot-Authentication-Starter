package com.example.authstarter.features.shared.utils;

import java.security.SecureRandom;
import java.util.Base64;

public final class TokenUtils {

    private static final int TOKEN_BYTES = 32;

    private TokenUtils() {}

    /** 32 random bytes, Base64URL without padding (43 chars, URL-safe). */
    public static String newToken(SecureRandom random) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}