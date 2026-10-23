package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.aquaventure.dto.surfer.UpsertProgressEntryRequest;
import com.example.aquaventure.entity.Booking;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.entity.ProgressEntry;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.SurferProfile;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.ConflictException;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.repository.ProgressEntryRepository;
import com.example.aquaventure.repository.SurferProfileRepository;
import com.example.aquaventure.repository.UserRepository;

class ProgressServiceTest {

    private final ProgressEntryRepository progressEntryRepository = mock(ProgressEntryRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SurferProfileRepository surferProfileRepository = mock(SurferProfileRepository.class);
    private final SurfLocationService surfLocationService = mock(SurfLocationService.class);
    private final SurfSchoolService surfSchoolService = mock(SurfSchoolService.class);

    private ProgressService progressService;

    @BeforeEach
    void setUp() {
        progressService = new ProgressService(progressEntryRepository, userRepository, surferProfileRepository,
                surfLocationService, surfSchoolService);
        when(progressEntryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private ProgressEntry entryFor(Long surferId) {
        User surfer = new User();
        surfer.setId(surferId);
        ProgressEntry entry = new ProgressEntry();
        entry.setId(1L);
        entry.setSurfer(surfer);
        entry.setDate(Instant.now());
        when(progressEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        return entry;
    }

    @Test
    void update_ownEntry_succeeds() {
        entryFor(5L);
        UpsertProgressEntryRequest request = new UpsertProgressEntryRequest();
        request.setDate(Instant.now());
        request.setSelfRating(4);

        var dto = progressService.update(5L, 1L, request);

        assertThat(dto.getSelfRating()).isEqualTo(4);
        assertThat(dto.isDraft()).isFalse();
    }

    @Test
    void update_someoneElsesEntry_throwsOwnershipViolation() {
        entryFor(5L);
        UpsertProgressEntryRequest request = new UpsertProgressEntryRequest();
        request.setDate(Instant.now());

        assertThatThrownBy(() -> progressService.update(999L, 1L, request))
                .isInstanceOf(OwnershipViolationException.class);
    }

    @Test
    void delete_someoneElsesEntry_throwsOwnershipViolation() {
        entryFor(5L);

        assertThatThrownBy(() -> progressService.delete(999L, 1L)).isInstanceOf(OwnershipViolationException.class);
    }

    @Test
    void summary_computesTotalsPerLocationAndRatingTrend() {
        SurfLocation arugam = new SurfLocation();
        arugam.setName("Arugam Bay");
        SurfLocation whisky = new SurfLocation();
        whisky.setName("Whisky Point");

        ProgressEntry e1 = entry(arugam, 90, 3, Instant.parse("2026-11-01T00:00:00Z"));
        ProgressEntry e2 = entry(arugam, 60, 4, Instant.parse("2026-11-03T00:00:00Z"));
        ProgressEntry e3 = entry(whisky, 45, null, Instant.parse("2026-11-02T00:00:00Z"));
        when(progressEntryRepository.findBySurferId(5L)).thenReturn(List.of(e1, e2, e3));

        SurferProfile profile = new SurferProfile();
        profile.setSkillLevel(SkillLevel.INTERMEDIATE);
        when(surferProfileRepository.findByUserId(5L)).thenReturn(Optional.of(profile));

        var summary = progressService.summary(5L);

        assertThat(summary.getTotalLessons()).isEqualTo(3);
        assertThat(summary.getTotalHoursInWater()).isEqualTo((90 + 60 + 45) / 60.0);
        assertThat(summary.getSessionsPerLocation()).containsEntry("Arugam Bay", 2L).containsEntry("Whisky Point", 1L);
        assertThat(summary.getRatingTrend()).hasSize(2);
        assertThat(summary.getRatingTrend().get(0).rating()).isEqualTo(3);
        assertThat(summary.getRatingTrend().get(1).rating()).isEqualTo(4);
        assertThat(summary.getCurrentSkillLevel()).isEqualTo(SkillLevel.INTERMEDIATE);
    }

    @Test
    void summary_noProfile_defaultsToBeginner() {
        when(progressEntryRepository.findBySurferId(5L)).thenReturn(List.of());
        when(surferProfileRepository.findByUserId(5L)).thenReturn(Optional.empty());

        var summary = progressService.summary(5L);

        assertThat(summary.getCurrentSkillLevel()).isEqualTo(SkillLevel.BEGINNER);
    }

    private ProgressEntry entry(SurfLocation location, int durationMinutes, Integer rating, Instant date) {
        ProgressEntry entry = new ProgressEntry();
        entry.setLocation(location);
        entry.setDurationMinutes(durationMinutes);
        entry.setSelfRating(rating);
        entry.setDate(date);
        return entry;
    }

    // --- instructor feedback authorization ---

    private ProgressEntry entryLinkedToBooking(Long schoolId, BookingStatus status) {
        SurfSchool school = new SurfSchool();
        school.setId(schoolId);
        Booking booking = new Booking();
        booking.setSchool(school);
        booking.setStatus(status);

        ProgressEntry entry = new ProgressEntry();
        entry.setId(2L);
        entry.setBooking(booking);
        when(progressEntryRepository.findById(2L)).thenReturn(Optional.of(entry));
        return entry;
    }

    @Test
    void addInstructorFeedback_owningSchoolOnCompletedBooking_succeeds() {
        entryLinkedToBooking(10L, BookingStatus.COMPLETED);
        SurfSchool ownedSchool = new SurfSchool();
        ownedSchool.setId(10L);
        when(surfSchoolService.getOwnedSchoolOrThrow(1L)).thenReturn(ownedSchool);

        var dto = progressService.addInstructorFeedback(1L, 2L, "Great progress!");

        assertThat(dto.getInstructorFeedback()).isEqualTo("Great progress!");
    }

    @Test
    void addInstructorFeedback_differentSchool_throwsOwnershipViolation() {
        entryLinkedToBooking(10L, BookingStatus.COMPLETED);
        SurfSchool otherSchool = new SurfSchool();
        otherSchool.setId(999L);
        when(surfSchoolService.getOwnedSchoolOrThrow(1L)).thenReturn(otherSchool);

        assertThatThrownBy(() -> progressService.addInstructorFeedback(1L, 2L, "note"))
                .isInstanceOf(OwnershipViolationException.class);
    }

    @Test
    void addInstructorFeedback_bookingNotCompleted_throwsConflict() {
        entryLinkedToBooking(10L, BookingStatus.PENDING);
        SurfSchool ownedSchool = new SurfSchool();
        ownedSchool.setId(10L);
        when(surfSchoolService.getOwnedSchoolOrThrow(1L)).thenReturn(ownedSchool);

        assertThatThrownBy(() -> progressService.addInstructorFeedback(1L, 2L, "note"))
                .isInstanceOf(ConflictException.class);
    }
}
