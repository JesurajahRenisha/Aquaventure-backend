package com.example.aquaventure.dto.booking;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateBookingRequest {

    @NotNull
    private Long lessonSlotId;

    @NotNull
    @Positive
    private Integer participants;

    private List<EquipmentLineItemRequest> equipmentItems;

    @Size(max = 1000)
    private String note;

    public Long getLessonSlotId() {
        return lessonSlotId;
    }

    public void setLessonSlotId(Long lessonSlotId) {
        this.lessonSlotId = lessonSlotId;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public List<EquipmentLineItemRequest> getEquipmentItems() {
        return equipmentItems;
    }

    public void setEquipmentItems(List<EquipmentLineItemRequest> equipmentItems) {
        this.equipmentItems = equipmentItems;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
