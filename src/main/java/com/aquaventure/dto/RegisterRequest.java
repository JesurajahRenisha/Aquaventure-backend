package com.aquaventure.dto;

import com.aquaventure.entity.Role;
import com.aquaventure.entity.SkillLevel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

    private String phoneNumber;

    @NotNull(message = "Role is required")
    private Role role;

    // TOURIST profile fields (optional)
    private SkillLevel skillLevel;
    private String experience;

    // PROVIDER profile fields (optional)
    private String businessName;
    private String contactDetails;
    private String location;

    // INSTRUCTOR profile fields -- an instructor account must belong to an
    // existing provider, so this is required when role=INSTRUCTOR
    private Long providerId;
}
