package com.example.authstarter.module.users.mapper;

import com.example.authstarter.module.users.dto.response.UserDetailsResponse;
import com.example.authstarter.module.users.dto.response.UserResponse;
import com.example.authstarter.module.users.model.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", expression = "java(payload.getEmail())")
    @Mapping(target = "firstName", expression = "java(payload.get(\"given_name\").toString())")
    @Mapping(target = "lastName", expression = "java(payload.get(\"family_name\").toString())")
    @Mapping(target = "picture", expression = "java(payload.get(\"picture\").toString())")
    @Mapping(target = "provider", expression = "java(\"GOOGLE\")")
    User toEntityFromGoogle(GoogleIdToken.Payload payload);

    UserResponse toDto(User user);

    UserDetailsResponse toDetailsDto(User user);
}
