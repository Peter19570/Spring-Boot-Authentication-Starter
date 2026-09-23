package com.example.authstarter.features.shared.exceptions.custom;

import com.example.authstarter.features.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AlreadyExistException extends AppException {
    public AlreadyExistException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
