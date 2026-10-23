package com.example.aquaventure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.aquaventure.entity.Instructor;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;

@DataJpaTest
@ActiveProfiles("test")
class LessonSlotRepositoryTest {

    @Autowired
    private LessonSlotRepository lessonSlotRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private InstructorRepository instructorRepository;
    @Autowired
    private SurfSchoolRepository surfSchoolRepository;
    @Autowired
    private UserRepository userRepository;

    private Instructor instructor;
    private Lesson lesson;

    private Instant baseStart;

    private User user(String email) {
        User u = new User();
        u.setFirstname("Owner");
        u.setLastname("Test");
        u.setEmail(email);
        u.setPassword("Hashed1!");
        u.setRole(Role.SCHOOL_ADMIN);
        return userRepository.saveAndFlush(u);
    }

    private void setUpFixtures() {
        User owner = user("overlap-owner@example.com");
        SurfSchool school = new SurfSchool();
        school.setOwner(owner);
        school.setName("Overlap Test School");
        school.setBusinessRegistrationNumber("BRN-X");
        school = surfSchoolRepository.saveAndFlush(school);

        instructor = new Instructor();
        instructor.setSchool(school);
        instructor.setName("Instructor X");
        instructor = instructorRepository.saveAndFlush(instructor);

        lesson = new Lesson();
        lesson.setSchool(school);
        lesson.setTitle("Beginner Group Lesson");
        lesson.setLessonType(LessonType.GROUP);
        lesson.setSkillLevel(SkillLevel.BEGINNER);
        lesson.setDurationMinutes(90);
        lesson.setPrice(java.math.BigDecimal.valueOf(50));
        lesson.setCurrency(Currency.USD);
        lesson.setMaxParticipants(6);
        lesson = lessonRepository.saveAndFlush(lesson);

        baseStart = Instant.parse("2026-11-01T08:00:00Z");
    }

    private LessonSlot slot(Instant start, Instant end) {
        LessonSlot s = new LessonSlot();
        s.setLesson(lesson);
        s.setInstructor(instructor);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setCapacity(5);
        return lessonSlotRepository.saveAndFlush(s);
    }

    @Test
    void existsOverlap_exactMatch_returnsTrue() {
        setUpFixtures();
        slot(baseStart, baseStart.plus(90, ChronoUnit.MINUTES));

        boolean overlap = lessonSlotRepository.existsOverlap(instructor.getId(), baseStart,
                baseStart.plus(90, ChronoUnit.MINUTES), null);

        assertThat(overlap).isTrue();
    }

    @Test
    void existsOverlap_partialOverlap_returnsTrue() {
        setUpFixtures();
        slot(baseStart, baseStart.plus(90, ChronoUnit.MINUTES));

        boolean overlap = lessonSlotRepository.existsOverlap(instructor.getId(),
                baseStart.plus(60, ChronoUnit.MINUTES), baseStart.plus(120, ChronoUnit.MINUTES), null);

        assertThat(overlap).isTrue();
    }

    @Test
    void existsOverlap_backToBack_returnsFalse() {
        setUpFixtures();
        slot(baseStart, baseStart.plus(90, ChronoUnit.MINUTES));

        boolean overlap = lessonSlotRepository.existsOverlap(instructor.getId(),
                baseStart.plus(90, ChronoUnit.MINUTES), baseStart.plus(180, ChronoUnit.MINUTES), null);

        assertThat(overlap).isFalse();
    }

    @Test
    void existsOverlap_excludesOwnIdOnUpdate() {
        setUpFixtures();
        LessonSlot existing = slot(baseStart, baseStart.plus(90, ChronoUnit.MINUTES));

        boolean overlap = lessonSlotRepository.existsOverlap(instructor.getId(), baseStart,
                baseStart.plus(90, ChronoUnit.MINUTES), existing.getId());

        assertThat(overlap).isFalse();
    }
}
