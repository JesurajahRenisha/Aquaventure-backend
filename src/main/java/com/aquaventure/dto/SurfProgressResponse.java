package com.aquaventure.dto;

import java.time.LocalDate;

import com.aquaventure.entity.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SurfProgressResponse {
    private Long progressId;
    private Long touristId;
    private Long instructorId;
    private String instructorName;
    private LocalDate sessionDate;
    private SkillLevel skillLevel;
    private String notes;
}
