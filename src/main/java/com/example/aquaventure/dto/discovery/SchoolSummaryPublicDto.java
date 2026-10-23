package com.example.aquaventure.dto.discovery;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.LessonType;

public class SchoolSummaryPublicDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String logoUrl;
    private List<String> locationNames;
    private Set<LessonType> lessonTypesOffered;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Currency currency;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public List<String> getLocationNames() {
        return locationNames;
    }

    public void setLocationNames(List<String> locationNames) {
        this.locationNames = locationNames;
    }

    public Set<LessonType> getLessonTypesOffered() {
        return lessonTypesOffered;
    }

    public void setLessonTypesOffered(Set<LessonType> lessonTypesOffered) {
        this.lessonTypesOffered = lessonTypesOffered;
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
}
