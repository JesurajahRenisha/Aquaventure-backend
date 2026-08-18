package com.aquaventure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminLoginResponse {
    private String token;
    private Long adminId;
    private String email;
    private final String role = "ADMIN";
}
