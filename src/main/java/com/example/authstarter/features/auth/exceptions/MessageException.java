package com.example.authstarter.features.auth.exceptions;

import com.example.authstarter.features.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class MessageException extends AppException {
    public MessageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
