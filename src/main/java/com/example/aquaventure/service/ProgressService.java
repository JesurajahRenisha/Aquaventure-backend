package com.example.aquaventure.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.surfer.ProgressEntryDto;
import com.example.aquaventure.dto.surfer.ProgressSummaryDto;
import com.example.aquaventure.dto.surfer.UpsertProgressEntryRequest;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.entity.ProgressEntry;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.exception.ConflictException;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.ProgressEntryMapper;
import com.example.aquaventure.repository.ProgressEntryRepository;
import com.example.aquaventure.repository.SurferProfileRepository;
import com.example.aquaventure.repository.UserRepository;

@Service
public class ProgressService {

    private final ProgressEntryRepository progressEntryRepository;
    private final UserRepository userRepository;
    private final SurferProfileRepository surferProfileRepository;
    private final SurfLocationService surfLocationService;
    private final SurfSchoolService surfSchoolService;

    public ProgressService(ProgressEntryRepository progressEntryRepository, UserRepository userRepository,
                            SurferProfileRepository surferProfileRepository, SurfLocationService surfLocationService,
                            SurfSchoolService surfSchoolService) {
        this.progressEntryRepository = progressEntryRepository;
        this.userRepository = userRepository;
        this.surferProfileRepository = surferProfileRepository;
        this.surfLocationService = surfLocationService;
        this.surfSchoolService = surfSchoolService;
    }

    @Transactional(readOnly = true)
    public Page<ProgressEntryDto> list(Long surferId, Pageable pageable) {
        return progressEntryRepository.findBySurferId(surferId, pageable).map(ProgressEntryMapper::toDto);
    }

    @Transactional
    public ProgressEntryDto create(Long surferId, UpsertProgressEntryRequest request) {
        ProgressEntry entry = new ProgressEntry();
        entry.setSurfer(userRepository.findById(surferId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + surferId)));
        apply(entry, request);
        return ProgressEntryMapper.toDto(progressEntryRepository.save(entry));
    }

    @Transactional
    public ProgressEntryDto update(Long surferId, Long entryId, UpsertProgressEntryRequest request) {
        ProgressEntry entry = getOwnedOrThrow(entryId, surferId);
        apply(entry, request);
        entry.setDraft(false);
        return ProgressEntryMapper.toDto(progressEntryRepository.save(entry));
    }

    @Transactional
    public void delete(Long surferId, Long entryId) {
        progressEntryRepository.delete(getOwnedOrThrow(entryId, surferId));
    }

    @Transactional
    public ProgressEntryDto addInstructorFeedback(Long ownerId, Long entryId, String feedback) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        ProgressEntry entry = progressEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress entry not found: " + entryId));

        if (entry.getBooking() == null || !entry.getBooking().getSchool().getId().equals(school.getId())) {
            throw new OwnershipViolationException("This progress entry is not linked to one of your school's bookings");
        }
        if (entry.getBooking().getStatus() != BookingStatus.COMPLETED) {
            throw new ConflictException("BOOKING_NOT_COMPLETED", "Feedback can only be added to completed bookings");
        }

        entry.setInstructorFeedback(feedback);
        return ProgressEntryMapper.toDto(progressEntryRepository.save(entry));
    }

    ProgressEntry getOwnedOrThrow(Long entryId, Long surferId) {
        ProgressEntry entry = progressEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress entry not found: " + entryId));
        if (!entry.getSurfer().getId().equals(surferId)) {
            throw new OwnershipViolationException("This progress entry does not belong to you");
        }
        return entry;
    }

    @Transactional(readOnly = true)
    public ProgressSummaryDto summary(Long surferId) {
        List<ProgressEntry> entries = progressEntryRepository.findBySurferId(surferId);

        ProgressSummaryDto dto = new ProgressSummaryDto();
        dto.setTotalLessons(entries.size());
        dto.setTotalHoursInWater(entries.stream()
                .filter(e -> e.getDurationMinutes() != null)
                .mapToInt(ProgressEntry::getDurationMinutes).sum() / 60.0);

        Map<String, Long> perLocation = new LinkedHashMap<>();
        for (ProgressEntry entry : entries) {
            String name = entry.getLocation() != null ? entry.getLocation().getName() : "Unknown";
            perLocation.merge(name, 1L, Long::sum);
        }
        dto.setSessionsPerLocation(perLocation);

        dto.setRatingTrend(entries.stream()
                .filter(e -> e.getSelfRating() != null)
                .sorted(Comparator.comparing(ProgressEntry::getDate))
                .map(e -> new ProgressSummaryDto.RatingPoint(e.getDate(), e.getSelfRating()))
                .collect(Collectors.toList()));

        dto.setCurrentSkillLevel(surferProfileRepository.findByUserId(surferId)
                .map(p -> p.getSkillLevel()).orElse(SkillLevel.BEGINNER));

        return dto;
    }

    private void apply(ProgressEntry entry, UpsertProgressEntryRequest request) {
        entry.setDate(request.getDate());
        entry.setLocation(request.getLocationId() != null
                ? surfLocationService.getByIdOrThrow(request.getLocationId()) : null);
        entry.setSkillFocus(request.getSkillFocus());
        entry.setDurationMinutes(request.getDurationMinutes());
        entry.setSelfRating(request.getSelfRating());
        entry.setNotes(request.getNotes());
    }
}
