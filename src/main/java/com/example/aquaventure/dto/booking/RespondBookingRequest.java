package com.example.aquaventure.dto.booking;

import jakarta.validation.constraints.Size;

public class RespondBookingRequest {

    @Size(max = 1000)
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
