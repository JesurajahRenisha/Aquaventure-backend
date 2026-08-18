package com.aquaventure.dto;

import com.aquaventure.entity.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SurfLocationResponse {
    private Long locationId;
    private String locationName;
    private SkillLevel difficultyLevel;
    private Integer safetyRating;
}
