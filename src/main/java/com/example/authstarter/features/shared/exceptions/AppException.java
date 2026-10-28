package com.example.authstarter.features.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AppException extends RuntimeException {
    private final HttpStatus status;

    protected AppException(String message, HttpStatus status) {
        this(message, status, null);
    }

    protected AppException(String message, HttpStatus status, Throwable cause) {
        // 4xx errors are expected flow (bad input, wrong password): skip the costly stack trace.
        // 5xx errors keep theirs so they can be debugged.
        super(message, cause, false, status.is5xxServerError());
        this.status = status;
    }
}