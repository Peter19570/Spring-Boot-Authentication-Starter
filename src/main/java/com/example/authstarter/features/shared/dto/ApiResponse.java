package com.example.authstarter.features.shared.dto;

public record ApiResponse<T>(
        String msg,
        T data
) {
}
