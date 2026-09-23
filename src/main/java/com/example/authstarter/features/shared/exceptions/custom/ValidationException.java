package com.example.authstarter.features.shared.exceptions.custom;

import com.example.authstarter.features.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
