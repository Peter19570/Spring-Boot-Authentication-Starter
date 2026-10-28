package com.example.authstarter.features.shared.exceptions.custom;

import com.example.authstarter.features.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class MessageException extends AppException {
    public MessageException(String message, Exception e) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, e);
    }
}
