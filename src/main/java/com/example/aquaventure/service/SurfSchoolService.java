package com.example.aquaventure.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.admin.SchoolDetailDto;
import com.example.aquaventure.dto.admin.SchoolSummaryDto;
import com.example.aquaventure.entity.NotificationType;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.InvalidStateTransitionException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.exception.SchoolNotApprovedException;
import com.example.aquaventure.mapper.SurfSchoolMapper;
import com.example.aquaventure.repository.SurfSchoolRepository;

@Service
public class SurfSchoolService {

    private final SurfSchoolRepository surfSchoolRepository;
    private final NotificationService notificationService;

    public SurfSchoolService(SurfSchoolRepository surfSchoolRepository, NotificationService notificationService) {
        this.surfSchoolRepository = surfSchoolRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public SurfSchool createPending(User owner, String name, String description, String address,
                                     String contactPhone, String contactEmail, String businessRegistrationNumber,
                                     List<SurfLocation> locations, List<String> documentUrls) {
        SurfSchool school = new SurfSchool();
        school.setOwner(owner);
        school.setName(name);
        school.setDescription(description);
        school.setAddress(address);
        school.setContactPhone(contactPhone);
        school.setContactEmail(contactEmail);
        school.setBusinessRegistrationNumber(businessRegistrationNumber);
        school.setStatus(SchoolStatus.PENDING);
        if (locations != null) {
            school.setLocations(Set.copyOf(locations));
        }
        if (documentUrls != null) {
            school.setVerificationDocumentUrls(documentUrls);
        }
        return surfSchoolRepository.save(school);
    }

    public SurfSchool getByIdOrThrow(Long id) {
        return surfSchoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surf school not found: " + id));
    }

    public SurfSchool findByOwnerUserId(Long ownerId) {
        return surfSchoolRepository.findByOwnerId(ownerId).orElse(null);
    }

    /** Resolves the caller's own school, regardless of status. Never trust a client-supplied school id. */
    public SurfSchool getOwnedSchoolOrThrow(Long ownerId) {
        return surfSchoolRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("No surf school found for this account"));
    }

    /** Resolves the caller's own school and asserts it is APPROVED. */
    public SurfSchool getOwnedApprovedSchoolOrThrow(Long ownerId) {
        SurfSchool school = getOwnedSchoolOrThrow(ownerId);
        if (school.getStatus() != SchoolStatus.APPROVED) {
            throw new SchoolNotApprovedException(
                    "Your school must be approved by the platform admin before you can manage this resource");
        }
        return school;
    }

    @Transactional(readOnly = true)
    public Page<SchoolSummaryDto> search(SchoolStatus status, Pageable pageable) {
        Page<SurfSchool> page = status != null
                ? surfSchoolRepository.findByStatus(status, pageable)
                : surfSchoolRepository.findAll(pageable);
        return page.map(SurfSchoolMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public SchoolDetailDto getDetail(Long id) {
        return SurfSchoolMapper.toDetailDto(getByIdOrThrow(id));
    }

    @Transactional
    public SurfSchool approve(Long id, User admin) {
        SurfSchool school = getByIdOrThrow(id);
        if (school.getStatus() != SchoolStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "Cannot approve a school in status " + school.getStatus());
        }
        school.setStatus(SchoolStatus.APPROVED);
        school.setApprovedAt(Instant.now());
        school.setApprovedBy(admin);
        school.setRejectionReason(null);
        surfSchoolRepository.save(school);
        notificationService.notify(school.getOwner(), NotificationType.SCHOOL_APPROVED,
                "Congratulations! Your surf school \"" + school.getName() + "\" has been approved.");
        return school;
    }

    @Transactional
    public SurfSchool reject(Long id, String reason) {
        SurfSchool school = getByIdOrThrow(id);
        if (school.getStatus() != SchoolStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "Cannot reject a school in status " + school.getStatus());
        }
        school.setStatus(SchoolStatus.REJECTED);
        school.setRejectionReason(reason);
        surfSchoolRepository.save(school);
        notificationService.notify(school.getOwner(), NotificationType.SCHOOL_REJECTED,
                "Your surf school application for \"" + school.getName() + "\" was rejected: " + reason);
        return school;
    }

    @Transactional
    public SurfSchool suspend(Long id) {
        SurfSchool school = getByIdOrThrow(id);
        if (school.getStatus() != SchoolStatus.APPROVED) {
            throw new InvalidStateTransitionException(
                    "Cannot suspend a school in status " + school.getStatus());
        }
        school.setStatus(SchoolStatus.SUSPENDED);
        surfSchoolRepository.save(school);
        notificationService.notify(school.getOwner(), NotificationType.SCHOOL_SUSPENDED,
                "Your surf school \"" + school.getName() + "\" has been suspended by the platform admin.");
        return school;
    }

    @Transactional
    public SurfSchool reactivate(Long id) {
        SurfSchool school = getByIdOrThrow(id);
        if (school.getStatus() != SchoolStatus.SUSPENDED) {
            throw new InvalidStateTransitionException(
                    "Cannot reactivate a school in status " + school.getStatus());
        }
        school.setStatus(SchoolStatus.APPROVED);
        surfSchoolRepository.save(school);
        notificationService.notify(school.getOwner(), NotificationType.SCHOOL_REACTIVATED,
                "Your surf school \"" + school.getName() + "\" has been reactivated.");
        return school;
    }
}
