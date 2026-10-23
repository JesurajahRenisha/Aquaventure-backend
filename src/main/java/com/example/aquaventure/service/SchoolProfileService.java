package com.example.aquaventure.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.school.SchoolProfileDto;
import com.example.aquaventure.dto.school.UpdateSchoolProfileRequest;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.exception.SchoolNotApprovedException;
import com.example.aquaventure.mapper.SurfSchoolMapper;
import com.example.aquaventure.repository.SurfSchoolRepository;

@Service
public class SchoolProfileService {

    private final SurfSchoolService surfSchoolService;
    private final SurfSchoolRepository surfSchoolRepository;
    private final SurfLocationService surfLocationService;

    public SchoolProfileService(SurfSchoolService surfSchoolService, SurfSchoolRepository surfSchoolRepository,
                                 SurfLocationService surfLocationService) {
        this.surfSchoolService = surfSchoolService;
        this.surfSchoolRepository = surfSchoolRepository;
        this.surfLocationService = surfLocationService;
    }

    @Transactional(readOnly = true)
    public SchoolProfileDto getOwnProfile(Long ownerId) {
        return SurfSchoolMapper.toProfileDto(surfSchoolService.getOwnedSchoolOrThrow(ownerId));
    }

    @Transactional
    public SchoolProfileDto updateOwnProfile(Long ownerId, UpdateSchoolProfileRequest request) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        if (school.getStatus() == SchoolStatus.SUSPENDED || school.getStatus() == SchoolStatus.REJECTED) {
            throw new SchoolNotApprovedException(
                    "Your school's profile cannot be edited while status is " + school.getStatus());
        }
        school.setName(request.getName());
        school.setDescription(request.getDescription());
        school.setAddress(request.getAddress());
        school.setContactPhone(request.getContactPhone());
        school.setContactEmail(request.getContactEmail());
        school.setLogoUrl(request.getLogoUrl());
        if (request.getImageUrls() != null) {
            school.setImageUrls(request.getImageUrls());
        }
        if (request.getLocationIds() != null) {
            List<SurfLocation> locations = request.getLocationIds().stream()
                    .map(surfLocationService::getByIdOrThrow).toList();
            Set<SurfLocation> locationSet = new HashSet<>(locations);
            school.setLocations(locationSet);
        }
        return SurfSchoolMapper.toProfileDto(surfSchoolRepository.save(school));
    }
}
