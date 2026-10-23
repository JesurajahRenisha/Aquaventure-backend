package com.example.aquaventure.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "surfer_profiles")
public class SurferProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel skillLevel = SkillLevel.BEGINNER;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_location_id")
    private SurfLocation preferredLocation;

    @Enumerated(EnumType.STRING)
    private LessonType preferredLessonType;

    private BigDecimal budgetMin;

    private BigDecimal budgetMax;

    @Size(max = 100)
    private String boardTypePreference;

    @Column(length = 2000)
    private String bio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public SurfLocation getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(SurfLocation preferredLocation) {
        this.preferredLocation = preferredLocation;
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
