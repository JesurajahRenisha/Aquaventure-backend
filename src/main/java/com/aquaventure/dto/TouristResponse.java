package com.aquaventure.dto;

import com.aquaventure.entity.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TouristResponse {
    private Long touristId;
    private Long userId;
    private String name;
    private String email;
    private SkillLevel skillLevel;
    private String experience;
}
