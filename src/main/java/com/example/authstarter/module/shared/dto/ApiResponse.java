package com.example.authstarter.module.shared.dto;

public record ApiResponse<T>(
        String msg,
        T data
) {
}
