package com.example.aquaventure.mapper;

import java.util.List;

import com.example.aquaventure.dto.booking.BookingDto;
import com.example.aquaventure.dto.booking.BookingEquipmentItemDto;
import com.example.aquaventure.entity.Booking;
import com.example.aquaventure.entity.BookingEquipmentItem;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingDto toDto(Booking booking, List<BookingEquipmentItem> items) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setSchoolId(booking.getSchool().getId());
        dto.setSchoolName(booking.getSchool().getName());
        dto.setLessonSlotId(booking.getLessonSlot().getId());
        dto.setLessonTitle(booking.getLessonSlot().getLesson().getTitle());
        dto.setSlotStartTime(booking.getLessonSlot().getStartTime());
        dto.setSlotEndTime(booking.getLessonSlot().getEndTime());
        dto.setInstructorId(booking.getInstructor().getId());
        dto.setInstructorName(booking.getInstructor().getName());
        dto.setParticipants(booking.getParticipants());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setCurrency(booking.getCurrency());
        dto.setSurferNote(booking.getSurferNote());
        dto.setStatus(booking.getStatus());
        dto.setSchoolResponseNote(booking.getSchoolResponseNote());
        dto.setRespondedAt(booking.getRespondedAt());
        dto.setCompletedAt(booking.getCompletedAt());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setEquipmentItems(items.stream().map(BookingMapper::toItemDto).toList());
        return dto;
    }

    private static BookingEquipmentItemDto toItemDto(BookingEquipmentItem item) {
        BookingEquipmentItemDto dto = new BookingEquipmentItemDto();
        dto.setEquipmentId(item.getEquipment().getId());
        dto.setEquipmentName(item.getEquipment().getName());
        dto.setQuantity(item.getQuantity());
        dto.setRentalUnit(item.getRentalUnit());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setLineTotal(item.getLineTotal());
        return dto;
    }
}
