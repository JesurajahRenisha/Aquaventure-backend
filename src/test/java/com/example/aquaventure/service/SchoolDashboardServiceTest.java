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
import com.example.aquaventure.entity.LessonSlotStatus;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.InstructorRepository;
import com.example.aquaventure.repository.LessonRepository;
import com.example.aquaventure.repository.LessonSlotRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SchoolDashboardServiceTest {

    @Autowired
    private SchoolDashboardService schoolDashboardService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurfSchoolRepository surfSchoolRepository;
    @Autowired
    private InstructorRepository instructorRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private LessonSlotRepository lessonSlotRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private SurfSchool school;
    private Instructor instructor;
    private Lesson lesson;

    private void setUpSchool() {
        owner = new User();
        owner.setFirstname("Dash");
        owner.setLastname("Owner");
        owner.setEmail("dash-owner@example.com");
        owner.setPassword("Hashed1!");
        owner.setRole(Role.SCHOOL_ADMIN);
        owner = userRepository.save(owner);

        school = new SurfSchool();
        school.setOwner(owner);
        school.setName("Dashboard School");
        school.setBusinessRegistrationNumber("BRN-DASH");
        school.setStatus(SchoolStatus.APPROVED);
        school = surfSchoolRepository.save(school);

        instructor = new Instructor();
        instructor.setSchool(school);
        instructor.setName("Dash Instructor");
        instructor = instructorRepository.save(instructor);

        lesson = new Lesson();
        lesson.setSchool(school);
        lesson.setTitle("Dash Lesson");
        lesson.setLessonType(LessonType.GROUP);
        lesson.setSkillLevel(SkillLevel.BEGINNER);
        lesson.setDurationMinutes(60);
        lesson.setPrice(BigDecimal.valueOf(50));
        lesson.setCurrency(Currency.USD);
        lesson.setMaxParticipants(5);
        lesson = lessonRepository.save(lesson);
    }

    private LessonSlot slot(Instant start, LessonSlotStatus status) {
        LessonSlot slot = new LessonSlot();
        slot.setLesson(lesson);
        slot.setInstructor(instructor);
        slot.setStartTime(start);
        slot.setEndTime(start.plus(1, ChronoUnit.HOURS));
        slot.setCapacity(5);
        slot.setStatus(status);
        return lessonSlotRepository.save(slot);
    }

    private Booking booking(LessonSlot slot, BookingStatus status, Instant completedAt, BigDecimal price) {
        User surfer = new User();
        surfer.setFirstname("Dash");
        surfer.setLastname("Surfer");
        surfer.setEmail("dash-surfer-" + System.nanoTime() + "@example.com");
        surfer.setPassword("Hashed1!");
        surfer.setRole(Role.SURFER);
        surfer = userRepository.save(surfer);

        Booking booking = new Booking();
        booking.setSurfer(surfer);
        booking.setSchool(school);
        booking.setLessonSlot(slot);
        booking.setInstructor(instructor);
        booking.setParticipants(1);
        booking.setTotalPrice(price);
        booking.setCurrency(Currency.USD);
        booking.setStatus(status);
        booking.setCompletedAt(completedAt);
        return bookingRepository.save(booking);
    }

    @Test
    void getDashboard_countsPendingBookingsAndUpcomingSlotsInOrder() {
        setUpSchool();
        LessonSlot pastSlot = slot(Instant.now().minus(2, ChronoUnit.DAYS), LessonSlotStatus.OPEN);
        LessonSlot soon = slot(Instant.now().plus(1, ChronoUnit.DAYS), LessonSlotStatus.OPEN);
        LessonSlot later = slot(Instant.now().plus(5, ChronoUnit.DAYS), LessonSlotStatus.OPEN);
        slot(Instant.now().plus(2, ChronoUnit.DAYS), LessonSlotStatus.CANCELLED);

        booking(pastSlot, BookingStatus.PENDING, null, BigDecimal.valueOf(50));
        booking(soon, BookingStatus.PENDING, null, BigDecimal.valueOf(50));

        var dashboard = schoolDashboardService.getDashboard(owner.getId());

        assertThat(dashboard.getPendingBookingsCount()).isEqualTo(2);
        assertThat(dashboard.getUpcomingLessons()).hasSize(2);
        assertThat(dashboard.getUpcomingLessons().get(0).getId()).isEqualTo(soon.getId());
        assertThat(dashboard.getUpcomingLessons().get(1).getId()).isEqualTo(later.getId());
    }

    @Test
    void getDashboard_revenueSumsOnlyCompletedBookings() {
        setUpSchool();
        LessonSlot slot1 = slot(Instant.now().plus(1, ChronoUnit.DAYS), LessonSlotStatus.OPEN);
        LessonSlot slot2 = slot(Instant.now().plus(2, ChronoUnit.DAYS), LessonSlotStatus.OPEN);

        booking(slot1, BookingStatus.COMPLETED, Instant.now(), BigDecimal.valueOf(100));
        booking(slot2, BookingStatus.PENDING, null, BigDecimal.valueOf(500));

        var dashboard = schoolDashboardService.getDashboard(owner.getId());

        assertThat(dashboard.getRevenue().getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(dashboard.getRevenue().getCompletedBookingsCount()).isEqualTo(1);
    }

    @Test
    void getDashboard_revenueThisMonth_excludesOldCompletedBookings() {
        setUpSchool();
        LessonSlot slot1 = slot(Instant.now().plus(1, ChronoUnit.DAYS), LessonSlotStatus.OPEN);

        booking(slot1, BookingStatus.COMPLETED, Instant.now().minus(60, ChronoUnit.DAYS), BigDecimal.valueOf(100));

        var dashboard = schoolDashboardService.getDashboard(owner.getId());

        assertThat(dashboard.getRevenue().getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(dashboard.getRevenue().getRevenueThisMonth()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
