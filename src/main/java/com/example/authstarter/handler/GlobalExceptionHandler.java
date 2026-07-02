package com.example.authstarter.handler;

import com.example.authstarter.features.auth.exceptions.AlreadyExistException;
import com.example.authstarter.features.auth.exceptions.AuthenticationException;
import com.example.authstarter.features.auth.exceptions.NotFoundException;
import com.example.authstarter.features.auth.exceptions.ValidationException;
import com.example.authstarter.features.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleServerException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal Server Error",
                        "Error caught: " + ex.getClass().getSimpleName()
                                + "-- Error Info: " + ex.getMessage()));
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ApiResponse<String>> handleConflictException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Conflict", ex.getMessage()));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler({
            AuthenticationException.class,
            NotFoundException.class,
            ValidationException.class
    })
    public ResponseEntity<ApiResponse<String>> handleBadException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Bad Request", ex.getMessage()));
    }
}
