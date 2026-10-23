package com.example.aquaventure.dto.school;

import java.math.BigDecimal;

import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.SkillLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpsertLessonRequest {

    @NotBlank
    @Size(max = 150)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull
    private LessonType lessonType;

    @NotNull
    private SkillLevel skillLevel;

    @NotNull
    @Positive
    private Integer durationMinutes;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    private Currency currency;

    @NotNull
    @Positive
    private Integer maxParticipants;

    private boolean includesEquipment = false;

    private Long locationId;

    private boolean active = true;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LessonType getLessonType() {
        return lessonType;
    }

    public void setLessonType(LessonType lessonType) {
        this.lessonType = lessonType;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public boolean isIncludesEquipment() {
        return includesEquipment;
    }

    public void setIncludesEquipment(boolean includesEquipment) {
        this.includesEquipment = includesEquipment;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
