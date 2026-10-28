package com.example.authstarter.features.shared.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        int numberOfElements,
        long totalElements,
        int totalPages,
        boolean isLast
) {
    public static <T> PageResponse<T> from (Page<T> page){
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1, // the API is 1-based (one-indexed-parameters=true), so report it that way
                page.getSize(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
