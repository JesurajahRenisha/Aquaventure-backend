package com.example.aquaventure.dto.discovery;

import java.util.List;

import com.example.aquaventure.dto.school.EquipmentDto;
import com.example.aquaventure.dto.school.InstructorDto;
import com.example.aquaventure.dto.school.LessonDto;

public class SchoolDetailPublicDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private String logoUrl;
    private List<String> imageUrls;
    private List<String> locationNames;
    private List<InstructorDto> instructors;
    private List<LessonDto> lessons;
    private List<EquipmentDto> equipment;

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

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getLocationNames() {
        return locationNames;
    }

    public void setLocationNames(List<String> locationNames) {
        this.locationNames = locationNames;
    }

    public List<InstructorDto> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<InstructorDto> instructors) {
        this.instructors = instructors;
    }

    public List<LessonDto> getLessons() {
        return lessons;
    }

    public void setLessons(List<LessonDto> lessons) {
        this.lessons = lessons;
    }

    public List<EquipmentDto> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<EquipmentDto> equipment) {
        this.equipment = equipment;
    }
}
