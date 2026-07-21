package com.example.authstarter.features.auth.mapper;

import com.example.authstarter.features.auth.dto.request.AuthRequest;
import com.example.authstarter.features.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "provider", constant = "LOCAL")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    User toEntityFromAuth(AuthRequest request, String firstName, String lastName);
}
