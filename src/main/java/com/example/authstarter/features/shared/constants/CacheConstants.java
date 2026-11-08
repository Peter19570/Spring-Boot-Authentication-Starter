package com.example.authstarter.features.shared.constants;

public final class CacheConstants {

    private CacheConstants() {}

    public static final String USERS = "users";

    public static final String[] CACHE_NAMES = {USERS};

    // Keeps the otp sent for password setup unique from the reset password
    public static final String PASSWORD_SETUP_OTP = "password-setup:";
}
