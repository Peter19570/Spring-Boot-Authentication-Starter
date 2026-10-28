package com.example.authstarter.features.auth.mapper;

import com.example.authstarter.features.auth.dto.internal.NameParts;
import com.example.authstarter.features.auth.dto.request.AuthRequest;
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
    User toEntityFromAuthRequest(AuthRequest request, NameParts names);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", expression = "java(payload.getEmail())")
    @Mapping(target = "firstName", expression = "java(claim(payload, \"given_name\"))")
    @Mapping(target = "lastName", expression = "java(claim(payload, \"family_name\"))")
    @Mapping(target = "picture", expression = "java(claim(payload, \"picture\"))")
    @Mapping(target = "provider", constant = "GOOGLE")
    @Mapping(target = "emailVerified", constant = "true")
    User toEntityFromGooglePayload(GoogleIdToken.Payload payload);

    // Same field mappings as above, but never touches the provider (AuthHelper.resolveAuthProviders owns that)
    @InheritConfiguration(name = "toEntityFromGooglePayload")
    @Mapping(target = "provider", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromGooglePayload(GoogleIdToken.Payload payload, @MappingTarget User user);

    // Google omits claims for some accounts (e.g. no family name); a missing claim must not crash login
    default String claim(GoogleIdToken.Payload payload, String name) {
        Object value = payload.get(name);
        return value != null ? value.toString() : null;
    }
}