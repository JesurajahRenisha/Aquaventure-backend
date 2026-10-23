package com.example.aquaventure.dto.surfer;

import java.time.Instant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpsertProgressEntryRequest {

    @NotNull
    private Instant date;

    private Long locationId;

    @Size(max = 150)
    private String skillFocus;

    private Integer durationMinutes;

    @Min(1)
    @Max(5)
    private Integer selfRating;

    @Size(max = 2000)
    private String notes;

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getSkillFocus() {
        return skillFocus;
    }

    public void setSkillFocus(String skillFocus) {
        this.skillFocus = skillFocus;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getSelfRating() {
        return selfRating;
    }

    public void setSelfRating(Integer selfRating) {
        this.selfRating = selfRating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
