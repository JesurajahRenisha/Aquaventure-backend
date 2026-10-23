package com.example.aquaventure.repository.spec;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.InstructorRepository;
import com.example.aquaventure.repository.LessonRepository;
import com.example.aquaventure.repository.LessonSlotRepository;
import com.example.aquaventure.repository.SurfLocationRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
class SurfSchoolSpecificationsTest {

    @Autowired
    private SurfSchoolRepository surfSchoolRepository;
    @Autowired
    private SurfLocationRepository surfLocationRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private LessonSlotRepository lessonSlotRepository;
    @Autowired
    private InstructorRepository instructorRepository;
    @Autowired
    private UserRepository userRepository;

    private SurfLocation arugamBay;
    private SurfLocation whiskyPoint;
    private SurfSchool approvedSchool;
    private SurfSchool pendingSchool;

    private User owner(String email) {
        User u = new User();
        u.setFirstname("Owner");
        u.setLastname("Test");
        u.setEmail(email);
        u.setPassword("Hashed1!");
        u.setRole(Role.SCHOOL_ADMIN);
        return userRepository.saveAndFlush(u);
    }

    @BeforeEach
    void setUp() {
        arugamBay = surfLocationRepository.saveAndFlush(location("Arugam Bay"));
        whiskyPoint = surfLocationRepository.saveAndFlush(location("Whisky Point"));

        approvedSchool = school("spec-approved@example.com", SchoolStatus.APPROVED, arugamBay);
        pendingSchool = school("spec-pending@example.com", SchoolStatus.PENDING, whiskyPoint);

        lesson(approvedSchool, LessonType.GROUP, SkillLevel.BEGINNER, BigDecimal.valueOf(40), arugamBay);
        lesson(pendingSchool, LessonType.PRIVATE, SkillLevel.ADVANCED, BigDecimal.valueOf(100), whiskyPoint);
    }

    private SurfLocation location(String name) {
        SurfLocation loc = new SurfLocation();
        loc.setName(name);
        loc.setLatitude(1.0);
        loc.setLongitude(1.0);
        return loc;
    }

    private SurfSchool school(String email, SchoolStatus status, SurfLocation location) {
        SurfSchool school = new SurfSchool();
        school.setOwner(owner(email));
        school.setName("School " + email);
        school.setBusinessRegistrationNumber("BRN-" + email);
        school.setStatus(status);
        school.setLocations(Set.of(location));
        return surfSchoolRepository.saveAndFlush(school);
    }

    private Lesson lesson(SurfSchool school, LessonType type, SkillLevel level, BigDecimal price, SurfLocation location) {
        Lesson lesson = new Lesson();
        lesson.setSchool(school);
        lesson.setTitle("Lesson for " + school.getName());
        lesson.setLessonType(type);
        lesson.setSkillLevel(level);
        lesson.setDurationMinutes(60);
        lesson.setPrice(price);
        lesson.setCurrency(Currency.USD);
        lesson.setMaxParticipants(5);
        lesson.setLocation(location);
        return lessonRepository.saveAndFlush(lesson);
    }

    @Test
    void isApproved_excludesNonApprovedSchools() {
        List<SurfSchool> result = surfSchoolRepository.findAll(SurfSchoolSpecifications.isApproved());

        assertThat(result).containsExactly(approvedSchool);
    }

    @Test
    void hasLocation_filtersToMatchingLocation() {
        List<SurfSchool> result = surfSchoolRepository.findAll(
                SurfSchoolSpecifications.isApproved().and(SurfSchoolSpecifications.hasLocation(arugamBay.getId())));

        assertThat(result).containsExactly(approvedSchool);

        List<SurfSchool> none = surfSchoolRepository.findAll(
                SurfSchoolSpecifications.isApproved().and(SurfSchoolSpecifications.hasLocation(whiskyPoint.getId())));
        assertThat(none).isEmpty();
    }

    @Test
    void hasMatchingLesson_filtersByLessonTypeSkillAndPrice() {
        Specification<SurfSchool> spec = SurfSchoolSpecifications.isApproved()
                .and(SurfSchoolSpecifications.hasMatchingLesson(LessonType.GROUP, SkillLevel.BEGINNER,
                        BigDecimal.valueOf(10), BigDecimal.valueOf(50)));

        assertThat(surfSchoolRepository.findAll(spec)).containsExactly(approvedSchool);

        Specification<SurfSchool> noMatch = SurfSchoolSpecifications.isApproved()
                .and(SurfSchoolSpecifications.hasMatchingLesson(LessonType.PRIVATE, null, null, null));
        assertThat(surfSchoolRepository.findAll(noMatch)).isEmpty();
    }

    @Test
    void hasMatchingLesson_priceOutOfRange_excludesSchool() {
        Specification<SurfSchool> spec = SurfSchoolSpecifications.isApproved()
                .and(SurfSchoolSpecifications.hasMatchingLesson(null, null, BigDecimal.valueOf(1000), null));

        assertThat(surfSchoolRepository.findAll(spec)).isEmpty();
    }

    @Test
    void availableOn_filtersByOpenSlotDate() {
        Lesson lesson = lessonRepository.findAll().stream()
                .filter(l -> l.getSchool().getId().equals(approvedSchool.getId())).findFirst().orElseThrow();

        LessonSlot slot = new LessonSlot();
        slot.setLesson(lesson);
        slot.setInstructor(instructorFor(approvedSchool));
        slot.setStartTime(Instant.parse("2026-12-01T09:00:00Z"));
        slot.setEndTime(Instant.parse("2026-12-01T10:30:00Z"));
        slot.setCapacity(5);
        lessonSlotRepository.saveAndFlush(slot);

        Specification<SurfSchool> spec = SurfSchoolSpecifications.isApproved()
                .and(SurfSchoolSpecifications.availableOn(LocalDate.of(2026, 12, 1)));
        assertThat(surfSchoolRepository.findAll(spec)).containsExactly(approvedSchool);

        Specification<SurfSchool> noMatch = SurfSchoolSpecifications.isApproved()
                .and(SurfSchoolSpecifications.availableOn(LocalDate.of(2026, 12, 2)));
        assertThat(surfSchoolRepository.findAll(noMatch)).isEmpty();
    }

    @Test
    void combinedFilters_allMustMatch() {
        Specification<SurfSchool> spec = SurfSchoolSpecifications.isApproved()
                .and(SurfSchoolSpecifications.hasLocation(arugamBay.getId()))
                .and(SurfSchoolSpecifications.hasMatchingLesson(LessonType.GROUP, SkillLevel.BEGINNER,
                        null, BigDecimal.valueOf(50)));

        assertThat(surfSchoolRepository.findAll(spec)).containsExactly(approvedSchool);
    }

    private com.example.aquaventure.entity.Instructor instructorFor(SurfSchool school) {
        com.example.aquaventure.entity.Instructor instructor = new com.example.aquaventure.entity.Instructor();
        instructor.setSchool(school);
        instructor.setName("Spec Instructor");
        return instructorRepository.saveAndFlush(instructor);
    }
}
