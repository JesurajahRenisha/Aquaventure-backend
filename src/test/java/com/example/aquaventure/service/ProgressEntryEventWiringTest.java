package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.aquaventure.dto.booking.CreateBookingRequest;
import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.Instructor;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.InstructorRepository;
import com.example.aquaventure.repository.LessonRepository;
import com.example.aquaventure.repository.LessonSlotRepository;
import com.example.aquaventure.repository.ProgressEntryRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.UserRepository;

/** Verifies the REAL Spring event wiring end to end - not calling the listener method directly. */
@SpringBootTest
@ActiveProfiles("test")
class ProgressEntryEventWiringTest {

    @Autowired
    private BookingService bookingService;
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
    private ProgressEntryRepository progressEntryRepository;

    @Test
    void completingABooking_reallyCreatesADraftProgressEntryViaTheEventBus() {
        User owner = new User();
        owner.setFirstname("Wiring");
        owner.setLastname("Owner");
        owner.setEmail("wiring-owner@example.com");
        owner.setPassword("Hashed1!");
        owner.setRole(Role.SCHOOL_ADMIN);
        owner = userRepository.save(owner);

        SurfSchool school = new SurfSchool();
        school.setOwner(owner);
        school.setName("Wiring School");
        school.setBusinessRegistrationNumber("BRN-WIRING");
        school.setStatus(SchoolStatus.APPROVED);
        school = surfSchoolRepository.save(school);

        Instructor instructor = new Instructor();
        instructor.setSchool(school);
        instructor.setName("Wiring Instructor");
        instructor = instructorRepository.save(instructor);

        Lesson lesson = new Lesson();
        lesson.setSchool(school);
        lesson.setTitle("Wiring Lesson");
        lesson.setLessonType(LessonType.GROUP);
        lesson.setSkillLevel(SkillLevel.BEGINNER);
        lesson.setDurationMinutes(60);
        lesson.setPrice(BigDecimal.valueOf(30));
        lesson.setCurrency(Currency.USD);
        lesson.setMaxParticipants(5);
        lesson = lessonRepository.save(lesson);

        LessonSlot slot = new LessonSlot();
        slot.setLesson(lesson);
        slot.setInstructor(instructor);
        slot.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        slot.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
        slot.setCapacity(5);
        slot = lessonSlotRepository.save(slot);

        User surfer = new User();
        surfer.setFirstname("Wiring");
        surfer.setLastname("Surfer");
        surfer.setEmail("wiring-surfer@example.com");
        surfer.setPassword("Hashed1!");
        surfer.setRole(Role.SURFER);
        surfer = userRepository.save(surfer);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setLessonSlotId(slot.getId());
        request.setParticipants(1);
        Long bookingId = bookingService.create(surfer.getId(), request).getId();
        bookingService.accept(owner.getId(), bookingId);
        bookingService.complete(owner.getId(), bookingId);

        assertThat(progressEntryRepository.findByBookingId(bookingId)).isPresent();
        assertThat(progressEntryRepository.findByBookingId(bookingId).get().isDraft()).isTrue();
    }
}
