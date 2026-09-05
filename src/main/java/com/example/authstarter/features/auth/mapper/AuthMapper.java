package com.example.authstarter.features.auth.mapper;

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
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    User toEntityFromAuthRequest(AuthRequest request, String firstName, String lastName);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", expression = "java(payload.getEmail())")
    @Mapping(target = "firstName", expression = "java(payload.get(\"given_name\").toString())")
    @Mapping(target = "lastName", expression = "java(payload.get(\"family_name\").toString())")
    @Mapping(target = "picture", expression = "java(payload.get(\"picture\").toString())")
    @Mapping(target = "provider", expression = "java(\"GOOGLE\")")
    User toEntityFromGooglePayload(GoogleIdToken.Payload payload);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", expression = "java(payload.getEmail())")
    @Mapping(target = "firstName", expression = "java(payload.get(\"given_name\").toString())")
    @Mapping(target = "lastName", expression = "java(payload.get(\"family_name\").toString())")
    @Mapping(target = "picture", expression = "java(payload.get(\"picture\").toString())")
    @Mapping(target = "emailVerified", constant = "true")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromGooglePayload(GoogleIdToken.Payload payload, @MappingTarget User user);
}
