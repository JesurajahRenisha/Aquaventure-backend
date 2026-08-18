package com.aquaventure.dto;

import com.aquaventure.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
    private boolean enabled;
}
