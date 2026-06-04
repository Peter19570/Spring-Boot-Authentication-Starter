package com.example.authstarter.handler;

import com.example.authstarter.module.auth.exceptions.AlreadyExistException;
import com.example.authstarter.module.auth.exceptions.AuthenticationException;
import com.example.authstarter.module.auth.exceptions.NotFoundException;
import com.example.authstarter.module.auth.exceptions.ValidationException;
import com.example.authstarter.module.shared.dto.ApiResponse;
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
                .body(new ApiResponse<>(
                        "Internal Server Error",
                        "Error caught: " + ex.getClass().getSimpleName()
                                + " Error Info: " + ex.getMessage()));
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ApiResponse<String>> handleConflictException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("Conflict", ex.getMessage()));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler({
            AuthenticationException.class,
            NotFoundException.class,
            ValidationException.class
    })
    public ResponseEntity<ApiResponse<String>> handleBadException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("Bad Request", ex.getMessage()));
    }
}
