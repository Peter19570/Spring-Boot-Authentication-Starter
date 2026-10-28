package com.example.authstarter.features.auth.constants;

public final class AsyncConstants {

    private AsyncConstants () {}

    public static final String EMAIL_EXECUTOR = "emailExecutor";

    public static final String EMAIL_THREAD = "EmailThread-";

    public static final String AUDIT_EXECUTOR = "auditExecutor";

    public static final String AUDIT_THREAD = "AuditThread-";

    public static final int AUDIT_CORE_POOL_SIZE = 2;

    public static final int AUDIT_MAX_POOL_SIZE = 4;

    public static final int AUDIT_QUEUE_CAPACITY = 1000;

    public static final int CORE_POOL_SIZE = 5;

    public static final int MAX_POOL_SIZE = 10;

    public static final int QUEUE_CAPACITY = 500;

}
