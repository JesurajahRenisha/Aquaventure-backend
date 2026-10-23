package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.surfer.RecommendationDto;
import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.SurferProfile;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.LessonRepository;
import com.example.aquaventure.repository.SurfLocationRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.SurferProfileRepository;
import com.example.aquaventure.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurferProfileRepository surferProfileRepository;
    @Autowired
    private SurfLocationRepository surfLocationRepository;
    @Autowired
    private SurfSchoolRepository surfSchoolRepository;
    @Autowired
    private LessonRepository lessonRepository;

    private SurfLocation arugamBay;
    private SurfSchool approvedSchool;
    private SurfSchool pendingSchool;

    @BeforeEach
    void setUp() {
        arugamBay = surfLocationRepository.save(location("Arugam Bay Rec"));

        approvedSchool = school("rec-approved@example.com", SchoolStatus.APPROVED);
        pendingSchool = school("rec-pending@example.com", SchoolStatus.PENDING);

        lesson(approvedSchool, SkillLevel.BEGINNER, true, arugamBay);
        lesson(approvedSchool, SkillLevel.ADVANCED, true, arugamBay);
        lesson(approvedSchool, SkillLevel.BEGINNER, false, arugamBay);
        lesson(pendingSchool, SkillLevel.BEGINNER, true, arugamBay);
    }

    private SurfLocation location(String name) {
        SurfLocation loc = new SurfLocation();
        loc.setName(name);
        loc.setLatitude(1.0);
        loc.setLongitude(1.0);
        return loc;
    }

    private SurfSchool school(String email, SchoolStatus status) {
        User owner = new User();
        owner.setFirstname("Owner");
        owner.setLastname("Test");
        owner.setEmail(email);
        owner.setPassword("Hashed1!");
        owner.setRole(Role.SCHOOL_ADMIN);
        owner = userRepository.save(owner);

        SurfSchool school = new SurfSchool();
        school.setOwner(owner);
        school.setName("School " + email);
        school.setBusinessRegistrationNumber("BRN-" + email);
        school.setStatus(status);
        return surfSchoolRepository.save(school);
    }

    private void lesson(SurfSchool school, SkillLevel level, boolean active, SurfLocation location) {
        Lesson lesson = new Lesson();
        lesson.setSchool(school);
        lesson.setTitle("Lesson " + level + " " + active);
        lesson.setLessonType(LessonType.GROUP);
        lesson.setSkillLevel(level);
        lesson.setDurationMinutes(60);
        lesson.setPrice(BigDecimal.valueOf(30));
        lesson.setCurrency(Currency.USD);
        lesson.setMaxParticipants(5);
        lesson.setLocation(location);
        lesson.setActive(active);
        lessonRepository.save(lesson);
    }

    private Long surferWithProfile(SkillLevel skillLevel, SurfLocation preferredLocation) {
        User surfer = new User();
        surfer.setFirstname("Surfer");
        surfer.setLastname("Test");
        surfer.setEmail("surfer-rec-" + System.nanoTime() + "@example.com");
        surfer.setPassword("Hashed1!");
        surfer.setRole(Role.SURFER);
        surfer = userRepository.save(surfer);

        SurferProfile profile = new SurferProfile();
        profile.setUser(surfer);
        profile.setSkillLevel(skillLevel);
        profile.setPreferredLocation(preferredLocation);
        surferProfileRepository.save(profile);
        return surfer.getId();
    }

    @Test
    void recommendFor_matchesSkillAndLocation_excludesInactiveAndNonApproved() {
        Long surferId = surferWithProfile(SkillLevel.BEGINNER, arugamBay);

        List<RecommendationDto> recommendations = recommendationService.recommendFor(surferId);

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.get(0).getSkillLevel()).isEqualTo(SkillLevel.BEGINNER);
        assertThat(recommendations.get(0).getSchoolId()).isEqualTo(approvedSchool.getId());
    }

    @Test
    void recommendFor_advancedSurfer_matchesAdvancedLessonOnly() {
        Long surferId = surferWithProfile(SkillLevel.ADVANCED, arugamBay);

        List<RecommendationDto> recommendations = recommendationService.recommendFor(surferId);

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.get(0).getSkillLevel()).isEqualTo(SkillLevel.ADVANCED);
    }

    @Test
    void recommendFor_noMatchingLocation_fallsBackToSkillOnlyMatch() {
        SurfLocation otherLocation = surfLocationRepository.save(location("Okanda Rec"));
        Long surferId = surferWithProfile(SkillLevel.BEGINNER, otherLocation);

        List<RecommendationDto> recommendations = recommendationService.recommendFor(surferId);

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.get(0).getMatchReason()).contains("skill level");
    }

    @Test
    void recommendFor_noProfile_returnsEmptyList() {
        User surfer = new User();
        surfer.setFirstname("No");
        surfer.setLastname("Profile");
        surfer.setEmail("no-profile-rec@example.com");
        surfer.setPassword("Hashed1!");
        surfer.setRole(Role.SURFER);
        surfer = userRepository.save(surfer);

        List<RecommendationDto> recommendations = recommendationService.recommendFor(surfer.getId());

        assertThat(recommendations).isEmpty();
    }
}
