package com.example.aquaventure.dto.school;

import java.util.List;
import java.util.Set;

import com.example.aquaventure.entity.SkillLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpsertInstructorRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 2000)
    private String bio;

    @Size(max = 500)
    private String photoUrl;

    private Integer experienceYears;

    private List<String> certifications;

    private List<String> languages;

    private Set<SkillLevel> skillLevelsTaught;

    private boolean active = true;

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
