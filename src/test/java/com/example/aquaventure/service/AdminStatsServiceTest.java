package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.entity.Booking;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.Instructor;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.InstructorRepository;
import com.example.aquaventure.repository.LessonRepository;
import com.example.aquaventure.repository.LessonSlotRepository;
import com.example.aquaventure.repository.SurfLocationRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminStatsServiceTest {

    @Autowired
    private AdminStatsService adminStatsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurfSchoolRepository surfSchoolRepository;
    @Autowired
    private SurfLocationRepository surfLocationRepository;
    @Autowired
    private InstructorRepository instructorRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private LessonSlotRepository lessonSlotRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void getStats_reflectsSeededFixtures() {
        userRepository.save(user("stats-surfer@example.com", Role.SURFER));
        userRepository.save(user("stats-admin@example.com", Role.PLATFORM_ADMIN));

        SurfLocation location = surfLocationRepository.save(location("Stats Spot"));

        SurfSchool approved = school("stats-approved@example.com", SchoolStatus.APPROVED);
        school("stats-pending@example.com", SchoolStatus.PENDING);

        Instructor instructor = new Instructor();
        instructor.setSchool(approved);
        instructor.setName("Stats Instructor");
        instructor = instructorRepository.save(instructor);

        Lesson lesson = new Lesson();
        lesson.setSchool(approved);
        lesson.setTitle("Stats Lesson");
        lesson.setLessonType(LessonType.GROUP);
        lesson.setSkillLevel(SkillLevel.BEGINNER);
        lesson.setDurationMinutes(60);
        lesson.setPrice(BigDecimal.valueOf(40));
        lesson.setCurrency(Currency.USD);
        lesson.setMaxParticipants(5);
        lesson.setLocation(location);
        lesson = lessonRepository.save(lesson);

        LessonSlot slot = new LessonSlot();
        slot.setLesson(lesson);
        slot.setInstructor(instructor);
        slot.setLocation(location);
        slot.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        slot.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
        slot.setCapacity(5);
        slot = lessonSlotRepository.save(slot);

        Booking booking = new Booking();
        booking.setSurfer(userRepository.findByEmail("stats-surfer@example.com").orElseThrow());
        booking.setSchool(approved);
        booking.setLessonSlot(slot);
        booking.setInstructor(instructor);
        booking.setParticipants(1);
        booking.setTotalPrice(BigDecimal.valueOf(40));
        booking.setCurrency(Currency.USD);
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        var stats = adminStatsService.getStats();

        assertThat(stats.getUsersByRole().get("SURFER")).isGreaterThanOrEqualTo(1);
        assertThat(stats.getUsersByRole().get("PLATFORM_ADMIN")).isGreaterThanOrEqualTo(1);
        assertThat(stats.getSchoolsByStatus().get("APPROVED")).isGreaterThanOrEqualTo(1);
        assertThat(stats.getSchoolsByStatus().get("PENDING")).isGreaterThanOrEqualTo(1);
        assertThat(stats.getBookingsByStatus().get("PENDING")).isGreaterThanOrEqualTo(1);
        assertThat(stats.getBookingsByLocation().get("Stats Spot")).isGreaterThanOrEqualTo(1);
    }

    private User user(String email, Role role) {
        User u = new User();
        u.setFirstname("Stats");
        u.setLastname("Test");
        u.setEmail(email);
        u.setPassword("Hashed1!");
        u.setRole(role);
        return u;
    }

    private SurfLocation location(String name) {
        SurfLocation loc = new SurfLocation();
        loc.setName(name);
        loc.setLatitude(1.0);
        loc.setLongitude(1.0);
        return loc;
    }

    private SurfSchool school(String email, SchoolStatus status) {
        User owner = userRepository.save(user(email, Role.SCHOOL_ADMIN));
        SurfSchool school = new SurfSchool();
        school.setOwner(owner);
        school.setName("School " + email);
        school.setBusinessRegistrationNumber("BRN-" + email);
        school.setStatus(status);
        return surfSchoolRepository.save(school);
    }
}
