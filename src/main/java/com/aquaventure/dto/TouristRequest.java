package com.aquaventure.dto;

import com.aquaventure.entity.SkillLevel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TouristRequest {
    private SkillLevel skillLevel;
    private String experience;
}
