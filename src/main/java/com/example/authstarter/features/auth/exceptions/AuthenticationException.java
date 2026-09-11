package com.example.authstarter.features.auth.exceptions;

import com.example.authstarter.features.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
