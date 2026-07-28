package com.example.authstarter.features.auth.dto.response;

public record NameParts(
        String firstName,
        String lastName
) {
    public static NameParts names(String firstName, String lastName){
        return new NameParts(firstName, lastName);
    }
}
