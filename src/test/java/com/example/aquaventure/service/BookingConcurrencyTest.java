package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.aquaventure.dto.booking.CreateBookingRequest;
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

/**
 * The highest-risk area called out in the implementation plan: concurrent accept() calls against
 * a capacity-limited slot must never over-book it. Runs real concurrent threads against the
 * actual (H2) database and real optimistic-locking retry logic - no mocks.
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingConcurrencyTest {

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
    private BookingRepository bookingRepository;

    @Test
    void concurrentAccepts_neverExceedSlotCapacity() throws Exception {
        User owner = user("concurrency-owner@example.com", Role.SCHOOL_ADMIN);
        SurfSchool school = new SurfSchool();
        school.setOwner(owner);
        school.setName("Concurrency School");
        school.setBusinessRegistrationNumber("BRN-CONC");
        school.setStatus(SchoolStatus.APPROVED);
        school = surfSchoolRepository.save(school);

        Instructor instructor = new Instructor();
        instructor.setSchool(school);
        instructor.setName("Concurrency Instructor");
        instructor = instructorRepository.save(instructor);

        Lesson lesson = new Lesson();
        lesson.setSchool(school);
        lesson.setTitle("Concurrency Lesson");
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
        slot.setStartTime(Instant.now().plus(5, ChronoUnit.DAYS));
        slot.setEndTime(Instant.now().plus(5, ChronoUnit.DAYS).plus(60, ChronoUnit.MINUTES));
        slot.setCapacity(5);
        slot = lessonSlotRepository.save(slot);
        Long slotId = slot.getId();

        int totalBookings = 8;
        List<Long> bookingIds = new ArrayList<>();
        for (int i = 0; i < totalBookings; i++) {
            User surfer = user("concurrency-surfer-" + i + "@example.com", Role.SURFER);
            CreateBookingRequest request = new CreateBookingRequest();
            request.setLessonSlotId(slotId);
            request.setParticipants(1);
            Long bookingId = bookingService.create(surfer.getId(), request).getId();
            bookingIds.add(bookingId);
        }

        ExecutorService executor = Executors.newFixedThreadPool(totalBookings);
        CountDownLatch readyLatch = new CountDownLatch(totalBookings);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        for (Long bookingId : bookingIds) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    bookingService.accept(owner.getId(), bookingId);
                    succeeded.incrementAndGet();
                } catch (Throwable t) {
                    failed.incrementAndGet();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        assertThat(succeeded.get()).isEqualTo(5);
        assertThat(failed.get()).isEqualTo(totalBookings - 5);

        LessonSlot reloaded = lessonSlotRepository.findById(slotId).orElseThrow();
        assertThat(reloaded.getBookedCount()).isEqualTo(5);
        assertThat(reloaded.getStatus()).isEqualTo(LessonSlotStatus.FULL);

        long acceptedCount = bookingIds.stream()
                .map(id -> bookingRepository.findById(id).orElseThrow())
                .filter(b -> b.getStatus() == com.example.aquaventure.entity.BookingStatus.ACCEPTED)
                .count();
        assertThat(acceptedCount).isEqualTo(5);
    }

    private User user(String email, Role role) {
        User u = new User();
        u.setFirstname("Conc");
        u.setLastname("Test");
        u.setEmail(email);
        u.setPassword("Hashed1!");
        u.setRole(role);
        return userRepository.save(u);
    }
}
