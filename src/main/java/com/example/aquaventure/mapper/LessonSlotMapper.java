package com.example.aquaventure.mapper;

import java.util.List;

import com.example.aquaventure.dto.school.LessonSlotDto;
import com.example.aquaventure.entity.LessonSlot;

public final class LessonSlotMapper {

    private LessonSlotMapper() {
    }

    public static LessonSlotDto toDto(LessonSlot slot) {
        LessonSlotDto dto = new LessonSlotDto();
        dto.setId(slot.getId());
        dto.setLessonId(slot.getLesson().getId());
        dto.setLessonTitle(slot.getLesson().getTitle());
        dto.setInstructorId(slot.getInstructor().getId());
        dto.setInstructorName(slot.getInstructor().getName());
        if (slot.getLocation() != null) {
            dto.setLocationId(slot.getLocation().getId());
            dto.setLocationName(slot.getLocation().getName());
        }
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setCapacity(slot.getCapacity());
        dto.setBookedCount(slot.getBookedCount());
        dto.setStatus(slot.getStatus());
        return dto;
    }

    public static List<LessonSlotDto> toDtoList(List<LessonSlot> slots) {
        return slots.stream().map(LessonSlotMapper::toDto).toList();
    }
}
