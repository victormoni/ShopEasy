package com.shopeasy.ecommerce.mapper;

import com.shopeasy.ecommerce.dto.response.UserResponse;
import com.shopeasy.ecommerce.model.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
