package com.aquaventure.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String certification;

    private String experience;

    private Boolean availability;
}
