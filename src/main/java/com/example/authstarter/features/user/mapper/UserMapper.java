package com.example.authstarter.features.user.mapper;

import com.example.authstarter.features.user.dto.response.UserDetailedResponse;
import com.example.authstarter.features.user.dto.response.UserResponse;
import com.example.authstarter.features.user.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toDto(User user);

    UserDetailedResponse toDetailedDto(User user);

}
