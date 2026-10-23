package com.example.aquaventure.mapper;

import com.example.aquaventure.dto.surfer.SurferProfileDto;
import com.example.aquaventure.entity.SurferProfile;

public final class SurferProfileMapper {

    private SurferProfileMapper() {
    }

    public static SurferProfileDto toDto(SurferProfile profile) {
        SurferProfileDto dto = new SurferProfileDto();
        dto.setId(profile.getId());
        dto.setSkillLevel(profile.getSkillLevel());
        if (profile.getPreferredLocation() != null) {
            dto.setPreferredLocationId(profile.getPreferredLocation().getId());
            dto.setPreferredLocationName(profile.getPreferredLocation().getName());
        }
        dto.setPreferredLessonType(profile.getPreferredLessonType());
        dto.setBudgetMin(profile.getBudgetMin());
        dto.setBudgetMax(profile.getBudgetMax());
        dto.setBoardTypePreference(profile.getBoardTypePreference());
        dto.setBio(profile.getBio());
        return dto;
    }
}
