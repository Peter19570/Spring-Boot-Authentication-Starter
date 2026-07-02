package com.example.authstarter.features.shared.dto;

public record ApiResponse<T>(
        String msg,
        T data
) {
    public static <T> ApiResponse<T> error(String msg, T data){
        return new ApiResponse<>("Error occurred: " + msg, data);
    }

    public static <T> ApiResponse<T> success(String msg, T data){
        return new ApiResponse<>(msg, data);
    }
}
