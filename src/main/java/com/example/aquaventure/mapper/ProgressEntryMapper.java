package com.example.aquaventure.mapper;

import com.example.aquaventure.dto.surfer.ProgressEntryDto;
import com.example.aquaventure.entity.ProgressEntry;

public final class ProgressEntryMapper {

    private ProgressEntryMapper() {
    }

    public static ProgressEntryDto toDto(ProgressEntry entry) {
        ProgressEntryDto dto = new ProgressEntryDto();
        dto.setId(entry.getId());
        dto.setBookingId(entry.getBooking() != null ? entry.getBooking().getId() : null);
        dto.setDate(entry.getDate());
        if (entry.getLocation() != null) {
            dto.setLocationId(entry.getLocation().getId());
            dto.setLocationName(entry.getLocation().getName());
        }
        dto.setSkillFocus(entry.getSkillFocus());
        dto.setDurationMinutes(entry.getDurationMinutes());
        dto.setSelfRating(entry.getSelfRating());
        dto.setNotes(entry.getNotes());
        dto.setInstructorFeedback(entry.getInstructorFeedback());
        dto.setDraft(entry.isDraft());
        return dto;
    }
}
