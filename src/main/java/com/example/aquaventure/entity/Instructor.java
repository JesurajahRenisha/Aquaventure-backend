package com.example.aquaventure.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "instructors")
public class Instructor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private SurfSchool school;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Column(length = 2000)
    private String bio;

    @Size(max = 500)
    private String photoUrl;

    private Integer experienceYears;

    @ElementCollection
    @CollectionTable(name = "instructor_certifications", joinColumns = @JoinColumn(name = "instructor_id"))
    @Column(name = "certification")
    private List<String> certifications = new java.util.ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "instructor_languages", joinColumns = @JoinColumn(name = "instructor_id"))
    @Column(name = "language")
    private List<String> languages = new java.util.ArrayList<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "instructor_skill_levels", joinColumns = @JoinColumn(name = "instructor_id"))
    @Column(name = "skill_level")
    private Set<SkillLevel> skillLevelsTaught = new HashSet<>();

    @NotNull
    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SurfSchool getSchool() {
        return school;
    }

    public void setSchool(SurfSchool school) {
        this.school = school;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public Set<SkillLevel> getSkillLevelsTaught() {
        return skillLevelsTaught;
    }

    public void setSkillLevelsTaught(Set<SkillLevel> skillLevelsTaught) {
        this.skillLevelsTaught = skillLevelsTaught;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
