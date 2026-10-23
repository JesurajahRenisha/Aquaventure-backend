package com.example.aquaventure.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.school.InstructorDto;
import com.example.aquaventure.dto.school.UpsertInstructorRequest;
import com.example.aquaventure.entity.Instructor;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.InstructorMapper;
import com.example.aquaventure.repository.InstructorRepository;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final SurfSchoolService surfSchoolService;

    public InstructorService(InstructorRepository instructorRepository, SurfSchoolService surfSchoolService) {
        this.instructorRepository = instructorRepository;
        this.surfSchoolService = surfSchoolService;
    }

    @Transactional(readOnly = true)
    public Page<InstructorDto> list(Long ownerId, Pageable pageable) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        return instructorRepository.findBySchoolId(school.getId(), pageable).map(InstructorMapper::toDto);
    }

    @Transactional
    public InstructorDto create(Long ownerId, UpsertInstructorRequest request) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Instructor instructor = new Instructor();
        instructor.setSchool(school);
        apply(instructor, request);
        return InstructorMapper.toDto(instructorRepository.save(instructor));
    }

    @Transactional
    public InstructorDto update(Long ownerId, Long instructorId, UpsertInstructorRequest request) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Instructor instructor = getOwnedOrThrow(instructorId, school.getId());
        apply(instructor, request);
        return InstructorMapper.toDto(instructorRepository.save(instructor));
    }

    @Transactional
    public void delete(Long ownerId, Long instructorId) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Instructor instructor = getOwnedOrThrow(instructorId, school.getId());
        instructorRepository.delete(instructor);
    }

    private Instructor getOwnedOrThrow(Long instructorId, Long schoolId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found: " + instructorId));
        if (!instructor.getSchool().getId().equals(schoolId)) {
            throw new OwnershipViolationException("This instructor does not belong to your school");
        }
        return instructor;
    }

    private void apply(Instructor instructor, UpsertInstructorRequest request) {
        instructor.setName(request.getName());
        instructor.setBio(request.getBio());
        instructor.setPhotoUrl(request.getPhotoUrl());
        instructor.setExperienceYears(request.getExperienceYears());
        if (request.getCertifications() != null) {
            instructor.setCertifications(request.getCertifications());
        }
        if (request.getLanguages() != null) {
            instructor.setLanguages(request.getLanguages());
        }
        if (request.getSkillLevelsTaught() != null) {
            instructor.setSkillLevelsTaught(request.getSkillLevelsTaught());
        }
        instructor.setActive(request.isActive());
    }
}
