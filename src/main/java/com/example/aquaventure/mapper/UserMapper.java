package com.example.aquaventure.mapper;

import com.example.aquaventure.dto.admin.UserSummaryDto;
import com.example.aquaventure.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserSummaryDto toSummaryDto(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getPhone(),
                user.getNationality());
    }
}
