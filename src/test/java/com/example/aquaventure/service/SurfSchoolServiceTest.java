package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.InvalidStateTransitionException;
import com.example.aquaventure.repository.SurfSchoolRepository;

class SurfSchoolServiceTest {

    private final SurfSchoolRepository surfSchoolRepository = mock(SurfSchoolRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private SurfSchoolService service;

    @BeforeEach
    void setUp() {
        service = new SurfSchoolService(surfSchoolRepository, notificationService);
        when(surfSchoolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private SurfSchool schoolWithStatus(SchoolStatus status) {
        SurfSchool school = new SurfSchool();
        school.setId(1L);
        school.setName("Test School");
        User owner = new User();
        owner.setId(10L);
        owner.setEmail("owner@example.com");
        school.setOwner(owner);
        school.setStatus(status);
        when(surfSchoolRepository.findById(1L)).thenReturn(Optional.of(school));
        return school;
    }

    @Test
    void approve_pendingSchool_becomesApproved() {
        schoolWithStatus(SchoolStatus.PENDING);
        User admin = new User();
        admin.setId(99L);

        SurfSchool result = service.approve(1L, admin);

        assertThat(result.getStatus()).isEqualTo(SchoolStatus.APPROVED);
        assertThat(result.getApprovedAt()).isNotNull();
    }

    @Test
    void approve_alreadyApproved_throws() {
        schoolWithStatus(SchoolStatus.APPROVED);

        assertThatThrownBy(() -> service.approve(1L, new User()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void reject_pendingSchool_becomesRejectedWithReason() {
        schoolWithStatus(SchoolStatus.PENDING);

        SurfSchool result = service.reject(1L, "Missing documents");

        assertThat(result.getStatus()).isEqualTo(SchoolStatus.REJECTED);
        assertThat(result.getRejectionReason()).isEqualTo("Missing documents");
    }

    @Test
    void suspend_approvedSchool_becomesSuspended() {
        schoolWithStatus(SchoolStatus.APPROVED);

        SurfSchool result = service.suspend(1L);

        assertThat(result.getStatus()).isEqualTo(SchoolStatus.SUSPENDED);
    }

    @Test
    void reactivate_suspendedSchool_becomesApproved() {
        schoolWithStatus(SchoolStatus.SUSPENDED);

        SurfSchool result = service.reactivate(1L);

        assertThat(result.getStatus()).isEqualTo(SchoolStatus.APPROVED);
    }
}
