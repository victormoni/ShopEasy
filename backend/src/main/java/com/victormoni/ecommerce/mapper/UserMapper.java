package com.victormoni.ecommerce.mapper;

import com.victormoni.ecommerce.dto.response.UserResponse;
import com.victormoni.ecommerce.model.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
