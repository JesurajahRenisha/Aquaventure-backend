package com.example.aquaventure.dto.discovery;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.SkillLevel;

public class SchoolCompareDto {

    private Long id;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Currency currency;
    private Set<LessonType> lessonTypesOffered;
    private Set<SkillLevel> skillLevelsSupported;
    private int instructorCount;
    private Set<String> languagesOffered;
    private boolean equipmentRentalAvailable;
    private List<String> locationNames;

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

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Set<LessonType> getLessonTypesOffered() {
        return lessonTypesOffered;
    }

    public void setLessonTypesOffered(Set<LessonType> lessonTypesOffered) {
        this.lessonTypesOffered = lessonTypesOffered;
    }

    public Set<SkillLevel> getSkillLevelsSupported() {
        return skillLevelsSupported;
    }

    public void setSkillLevelsSupported(Set<SkillLevel> skillLevelsSupported) {
        this.skillLevelsSupported = skillLevelsSupported;
    }

    public int getInstructorCount() {
        return instructorCount;
    }

    public void setInstructorCount(int instructorCount) {
        this.instructorCount = instructorCount;
    }

    public Set<String> getLanguagesOffered() {
        return languagesOffered;
    }

    public void setLanguagesOffered(Set<String> languagesOffered) {
        this.languagesOffered = languagesOffered;
    }

    public boolean isEquipmentRentalAvailable() {
        return equipmentRentalAvailable;
    }

    public void setEquipmentRentalAvailable(boolean equipmentRentalAvailable) {
        this.equipmentRentalAvailable = equipmentRentalAvailable;
    }

    public List<String> getLocationNames() {
        return locationNames;
    }

    public void setLocationNames(List<String> locationNames) {
        this.locationNames = locationNames;
    }
}
