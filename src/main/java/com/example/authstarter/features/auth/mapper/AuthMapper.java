package com.example.authstarter.features.auth.mapper;

import com.example.authstarter.features.auth.dto.internal.NameParts;
import com.example.authstarter.features.auth.dto.request.RegisterRequest;
import com.example.authstarter.features.user.model.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "provider", constant = "LOCAL")
    @Mapping(target = "firstName", source = "names.firstName")
    @Mapping(target = "lastName", source = "names.lastName")
    User toEntityFromAuthRequest(RegisterRequest request, NameParts names);

    // Google's payload is a raw Map, which MapStruct can't introspect, so these two are written by hand.
    default User toEntityFromGooglePayload(GoogleIdToken.Payload payload) {
        User user = new User();
        user.setEmail(payload.getEmail());
        user.setFirstName(claim(payload, "given_name"));
        user.setLastName(claim(payload, "family_name"));
        user.setPicture(claim(payload, "picture"));
        user.setProvider("GOOGLE");
        user.setEmailVerified(true);
        return user;
    }

    // Only overwrites a field when Google actually sent that claim; never touches the provider
    default void updateEntityFromGooglePayload(GoogleIdToken.Payload payload, User user) {
        String firstName = claim(payload, "given_name");
        String lastName = claim(payload, "family_name");
        String picture = claim(payload, "picture");

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (picture != null) user.setPicture(picture);
        user.setEmailVerified(true);
    }

    // Google omits claims for some accounts (e.g. no family name); a missing claim must not crash login
    private static String claim(GoogleIdToken.Payload payload, String name) {
        Object value = payload.get(name);
        return value != null ? value.toString() : null;
    }
}