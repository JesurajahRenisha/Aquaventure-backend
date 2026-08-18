package com.aquaventure.dto;

import java.time.LocalDate;

import com.aquaventure.entity.SkillLevel;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurfProgressRequest {

    @NotNull(message = "Tourist is required")
    private Long touristId;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    private SkillLevel skillLevel;

    private String notes;
}
