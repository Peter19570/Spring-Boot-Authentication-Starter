package com.example.authstarter.features.shared.constants;

public final class AsyncConstants {

    private AsyncConstants () {}

    public static final String EMAIL_EXECUTOR = "emailExecutor";

    public static final String EMAIL_THREAD = "EmailThread-";

    public static final String AUDIT_EXECUTOR = "auditExecutor";

    public static final String AUDIT_THREAD = "AuditThread-";

    public static final int EMAIL_CORE_POOL_SIZE = 5;

    public static final int EMAIL_MAX_POOL_SIZE = 10;

    public static final int EMAIL_QUEUE_CAPACITY = 500;

    public static final int AUDIT_CORE_POOL_SIZE = 2;

    public static final int AUDIT_MAX_POOL_SIZE = 4;

    public static final int AUDIT_QUEUE_CAPACITY = 1000;

}
