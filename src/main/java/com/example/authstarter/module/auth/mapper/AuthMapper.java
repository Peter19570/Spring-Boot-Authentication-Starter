package com.example.authstarter.module.auth.mapper;

import com.example.authstarter.module.auth.dto.request.AuthRequest;
import com.example.authstarter.module.users.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntityFromAuth(AuthRequest request);
}
