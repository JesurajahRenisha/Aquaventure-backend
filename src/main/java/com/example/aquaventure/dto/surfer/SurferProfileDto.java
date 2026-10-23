package com.example.aquaventure.dto.surfer;

import java.math.BigDecimal;

import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.SkillLevel;

public class SurferProfileDto {

    private Long id;
    private SkillLevel skillLevel;
    private Long preferredLocationId;
    private String preferredLocationName;
    private LessonType preferredLessonType;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String boardTypePreference;
    private String bio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Long getPreferredLocationId() {
        return preferredLocationId;
    }

    public void setPreferredLocationId(Long preferredLocationId) {
        this.preferredLocationId = preferredLocationId;
    }

    public String getPreferredLocationName() {
        return preferredLocationName;
    }

    public void setPreferredLocationName(String preferredLocationName) {
        this.preferredLocationName = preferredLocationName;
    }

    public LessonType getPreferredLessonType() {
        return preferredLessonType;
    }

    public void setPreferredLessonType(LessonType preferredLessonType) {
        this.preferredLessonType = preferredLessonType;
    }

    public BigDecimal getBudgetMin() {
        return budgetMin;
    }

    public void setBudgetMin(BigDecimal budgetMin) {
        this.budgetMin = budgetMin;
    }

    public BigDecimal getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(BigDecimal budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getBoardTypePreference() {
        return boardTypePreference;
    }

    public void setBoardTypePreference(String boardTypePreference) {
        this.boardTypePreference = boardTypePreference;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
