package com.aquaventure.dto;

import com.aquaventure.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;
    /** The id of the role-specific profile row (Tourist/Provider/Instructor), if any. */
    private Long profileId;
}
