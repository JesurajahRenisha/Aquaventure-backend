package com.example.aquaventure.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.example.aquaventure.dto.school.InstructorDto;
import com.example.aquaventure.entity.Instructor;

public final class InstructorMapper {

    private InstructorMapper() {
    }

    public static InstructorDto toDto(Instructor instructor) {
        InstructorDto dto = new InstructorDto();
        dto.setId(instructor.getId());
        dto.setName(instructor.getName());
        dto.setBio(instructor.getBio());
        dto.setPhotoUrl(instructor.getPhotoUrl());
        dto.setExperienceYears(instructor.getExperienceYears());
        dto.setCertifications(new ArrayList<>(instructor.getCertifications()));
        dto.setLanguages(new ArrayList<>(instructor.getLanguages()));
        dto.setSkillLevelsTaught(new HashSet<>(instructor.getSkillLevelsTaught()));
        dto.setActive(instructor.isActive());
        return dto;
    }

    public static List<InstructorDto> toDtoList(List<Instructor> instructors) {
        return instructors.stream().map(InstructorMapper::toDto).toList();
    }
}
