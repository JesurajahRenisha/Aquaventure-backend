package com.aquaventure.mapper;

import com.aquaventure.dto.UserResponse;
import com.aquaventure.entity.User;

public final class AuthMapper {

    private AuthMapper() {
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isEnabled());
    }
}
