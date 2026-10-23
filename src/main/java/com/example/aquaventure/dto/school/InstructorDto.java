package com.example.aquaventure.dto.school;

import java.util.List;
import java.util.Set;

import com.example.aquaventure.entity.SkillLevel;

public class InstructorDto {

    private Long id;
    private String name;
    private String bio;
    private String photoUrl;
    private Integer experienceYears;
    private List<String> certifications;
    private List<String> languages;
    private Set<SkillLevel> skillLevelsTaught;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
