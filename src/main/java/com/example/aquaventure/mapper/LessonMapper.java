package com.example.aquaventure.mapper;

import java.util.List;

import com.example.aquaventure.dto.school.LessonDto;
import com.example.aquaventure.entity.Lesson;

public final class LessonMapper {

    private LessonMapper() {
    }

    public static LessonDto toDto(Lesson lesson) {
        LessonDto dto = new LessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setDescription(lesson.getDescription());
        dto.setLessonType(lesson.getLessonType());
        dto.setSkillLevel(lesson.getSkillLevel());
        dto.setDurationMinutes(lesson.getDurationMinutes());
        dto.setPrice(lesson.getPrice());
        dto.setCurrency(lesson.getCurrency());
        dto.setMaxParticipants(lesson.getMaxParticipants());
        dto.setIncludesEquipment(lesson.isIncludesEquipment());
        if (lesson.getLocation() != null) {
            dto.setLocationId(lesson.getLocation().getId());
            dto.setLocationName(lesson.getLocation().getName());
        }
        dto.setActive(lesson.isActive());
        return dto;
    }

    public static List<LessonDto> toDtoList(List<Lesson> lessons) {
        return lessons.stream().map(LessonMapper::toDto).toList();
    }
}
