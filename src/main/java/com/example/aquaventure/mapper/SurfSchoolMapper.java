package com.example.aquaventure.mapper;

import java.util.ArrayList;
import java.util.List;

import com.example.aquaventure.dto.admin.SchoolDetailDto;
import com.example.aquaventure.dto.admin.SchoolSummaryDto;
import com.example.aquaventure.dto.school.SchoolProfileDto;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;

public final class SurfSchoolMapper {

    private SurfSchoolMapper() {
    }

    public static SchoolSummaryDto toSummaryDto(SurfSchool school) {
        return new SchoolSummaryDto(
                school.getId(),
                school.getName(),
                school.getAddress(),
                school.getStatus(),
                school.getOwner() != null ? school.getOwner().getEmail() : null);
    }

    /** Admin-facing detail: includes sensitive fields (registration number, documents, rejection reason). */
    public static SchoolDetailDto toDetailDto(SurfSchool school) {
        SchoolDetailDto dto = new SchoolDetailDto();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setDescription(school.getDescription());
        dto.setAddress(school.getAddress());
        dto.setContactPhone(school.getContactPhone());
        dto.setContactEmail(school.getContactEmail());
        dto.setBusinessRegistrationNumber(school.getBusinessRegistrationNumber());
        dto.setLogoUrl(school.getLogoUrl());
        dto.setVerificationDocumentUrls(new ArrayList<>(school.getVerificationDocumentUrls()));
        dto.setImageUrls(new ArrayList<>(school.getImageUrls()));
        dto.setStatus(school.getStatus());
        dto.setRejectionReason(school.getRejectionReason());
        dto.setApprovedAt(school.getApprovedAt());
        dto.setOwnerEmail(school.getOwner() != null ? school.getOwner().getEmail() : null);
        dto.setLocationNames(school.getLocations().stream().map(SurfLocation::getName).toList());
        return dto;
    }

    public static List<SchoolSummaryDto> toSummaryDtoList(List<SurfSchool> schools) {
        return schools.stream().map(SurfSchoolMapper::toSummaryDto).toList();
    }

    /** School-admin-facing own profile: no other school's data, but includes own sensitive fields. */
    public static SchoolProfileDto toProfileDto(SurfSchool school) {
        SchoolProfileDto dto = new SchoolProfileDto();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setDescription(school.getDescription());
        dto.setAddress(school.getAddress());
        dto.setContactPhone(school.getContactPhone());
        dto.setContactEmail(school.getContactEmail());
        dto.setLogoUrl(school.getLogoUrl());
        dto.setImageUrls(new ArrayList<>(school.getImageUrls()));
        dto.setStatus(school.getStatus());
        dto.setRejectionReason(school.getRejectionReason());
        dto.setLocationIds(school.getLocations().stream().map(SurfLocation::getId).toList());
        dto.setLocationNames(school.getLocations().stream().map(SurfLocation::getName).toList());
        return dto;
    }
}
