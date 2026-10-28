package com.example.authstarter.features.auth.dto.response;

public record TokenResponse(
        String access,
        String refresh,
        long expiresIn // access token lifetime in seconds
){
    @Override
    public String toString() {
        return "TokenResponse[expiresIn=" + expiresIn + "]"; // never print tokens into logs
    }
}