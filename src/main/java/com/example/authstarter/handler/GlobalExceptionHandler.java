package com.example.authstarter.handler;

import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice(basePackages = "com.example.authstarter.features")
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleServerException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Internal Server Error (500)",
                        "Error caught: " + ex.getClass().getSimpleName() + " -- Error Info: " + ex.getMessage()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex){
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getStatus().getReasonPhrase(), ex.getMessage()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<ApiResponse<String>> handleBadRequest(Exception ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Bad Request (400)", ex.getMessage()));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            AccessDeniedException.class,
            AuthorizationDeniedException.class
    })
    public ResponseEntity<ApiResponse<String>> handleAuthException(Exception ex){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized (401)", ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodNotAllowed(Exception ex){
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("Method Not Allowed (405)", ex.getMessage()));
    }

}
