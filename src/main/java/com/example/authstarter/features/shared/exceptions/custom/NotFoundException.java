package com.example.authstarter.features.shared.exceptions.custom;

import com.example.authstarter.features.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
