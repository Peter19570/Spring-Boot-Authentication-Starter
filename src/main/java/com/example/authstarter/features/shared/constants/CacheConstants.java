package com.example.authstarter.features.shared.constants;

public final class CacheConstants {

    private CacheConstants() {}

    public static final String USER = "users";

    public static final String ALL_USERS = "allUsers";

    public static final String PAGE_CACHE_KEY = "#pagaable.pageNumber '-' #pagable.pageSize";

    public static final String[] CACHE_NAMES = {USER, ALL_USERS};
}
