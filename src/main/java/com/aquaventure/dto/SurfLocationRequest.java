package com.aquaventure.dto;

import com.aquaventure.entity.SkillLevel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurfLocationRequest {

    @NotBlank(message = "Location name is required")
    private String locationName;

    @NotNull(message = "Difficulty level is required")
    private SkillLevel difficultyLevel;

    @Min(value = 1, message = "Safety rating must be between 1 and 5")
    @Max(value = 5, message = "Safety rating must be between 1 and 5")
    private Integer safetyRating;
}
