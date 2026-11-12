package com.example.authstarter.features.auth.constants;

import java.time.Duration;

public final class HelperConstants {

    private HelperConstants () {}

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);
}
