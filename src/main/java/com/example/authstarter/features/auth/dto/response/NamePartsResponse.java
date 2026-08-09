package com.example.authstarter.features.auth.dto.response;

public record NamePartsResponse(
        String firstName,
        String lastName
) {
    public static NamePartsResponse names(String firstName, String lastName){
        return new NamePartsResponse(firstName, lastName);
    }
}
