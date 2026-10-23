package com.example.aquaventure.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.UserRepository;
import com.example.aquaventure.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
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
    private JwtUtil jwtUtil;

    private User user(String email, Role role) {
        User u = new User();
        u.setFirstname("Test");
        u.setLastname("User");
        u.setEmail(email);
        u.setPassword("Hashed1!");
        u.setRole(role);
        return userRepository.save(u);
    }

    private record TestSlot(Long slotId, User schoolOwner) {
    }

    private TestSlot slotFor(String prefix) {
        User owner = user(prefix.toLowerCase() + "-owner@example.com", Role.SCHOOL_ADMIN);
        SurfSchool school = new SurfSchool();
        school.setOwner(owner);
        school.setName(prefix + " School");
        school.setBusinessRegistrationNumber("BRN-" + prefix);
        school.setStatus(SchoolStatus.APPROVED);
        school = surfSchoolRepository.save(school);

        Instructor instructor = new Instructor();
        instructor.setSchool(school);
        instructor.setName(prefix + " Instructor");
        instructor = instructorRepository.save(instructor);

        Lesson lesson = new Lesson();
        lesson.setSchool(school);
        lesson.setTitle(prefix + " Lesson");
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
        slot.setStartTime(Instant.now().plus(3, ChronoUnit.DAYS));
        slot.setEndTime(Instant.now().plus(3, ChronoUnit.DAYS).plus(60, ChronoUnit.MINUTES));
        slot.setCapacity(5);
        slot = lessonSlotRepository.save(slot);

        return new TestSlot(slot.getId(), owner);
    }

    private String auth(User u) {
        return "Bearer " + jwtUtil.generateToken(u);
    }

    @Test
    void fullBookingLifecycle_createAcceptComplete() throws Exception {
        TestSlot testSlot = slotFor("Lifecycle");
        User surfer = user("lifecycle-surfer@example.com", Role.SURFER);

        String createBody = "{\"lessonSlotId\":" + testSlot.slotId() + ",\"participants\":1}";
        String response = mockMvc.perform(post("/api/bookings")
                        .header(HttpHeaders.AUTHORIZATION, auth(surfer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andReturn().getResponse().getContentAsString();
        Long bookingId = extractId(response);

        mockMvc.perform(patch("/api/school/me/bookings/" + bookingId + "/accept")
                        .header(HttpHeaders.AUTHORIZATION, auth(testSlot.schoolOwner())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("ACCEPTED")));

        mockMvc.perform(patch("/api/school/me/bookings/" + bookingId + "/complete")
                        .header(HttpHeaders.AUTHORIZATION, auth(testSlot.schoolOwner())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));

        mockMvc.perform(get("/api/notifications/me").header(HttpHeaders.AUTHORIZATION, auth(surfer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void crossSchool_acceptOtherSchoolsBooking_returns403() throws Exception {
        TestSlot schoolA = slotFor("CrossA");
        TestSlot schoolB = slotFor("CrossB");
        User surfer = user("cross-surfer@example.com", Role.SURFER);

        String createBody = "{\"lessonSlotId\":" + schoolA.slotId() + ",\"participants\":1}";
        String response = mockMvc.perform(post("/api/bookings")
                        .header(HttpHeaders.AUTHORIZATION, auth(surfer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long bookingId = extractId(response);

        mockMvc.perform(patch("/api/school/me/bookings/" + bookingId + "/accept")
                        .header(HttpHeaders.AUTHORIZATION, auth(schoolB.schoolOwner())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN_OWNERSHIP")));
    }

    @Test
    void cancelBooking_surferCanOnlyCancelOwnBooking() throws Exception {
        TestSlot testSlot = slotFor("CancelOwn");
        User surfer = user("cancel-surfer@example.com", Role.SURFER);
        User otherSurfer = user("other-surfer@example.com", Role.SURFER);

        String createBody = "{\"lessonSlotId\":" + testSlot.slotId() + ",\"participants\":1}";
        String response = mockMvc.perform(post("/api/bookings")
                        .header(HttpHeaders.AUTHORIZATION, auth(surfer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long bookingId = extractId(response);

        mockMvc.perform(patch("/api/surfer/me/bookings/" + bookingId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, auth(otherSurfer)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/surfer/me/bookings/" + bookingId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, auth(surfer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));
    }

    @Test
    void notifications_readAll_onlyAffectsOwnNotifications() throws Exception {
        TestSlot testSlot = slotFor("NotifyScope");
        User surfer = user("notify-surfer@example.com", Role.SURFER);

        String createBody = "{\"lessonSlotId\":" + testSlot.slotId() + ",\"participants\":1}";
        mockMvc.perform(post("/api/bookings")
                        .header(HttpHeaders.AUTHORIZATION, auth(surfer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        // the school owner received a BOOKING_CREATED notification; the surfer has none yet
        mockMvc.perform(get("/api/notifications/me").header(HttpHeaders.AUTHORIZATION, auth(testSlot.schoolOwner())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(1)));

        mockMvc.perform(get("/api/notifications/me").header(HttpHeaders.AUTHORIZATION, auth(surfer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(0)));

        mockMvc.perform(patch("/api/notifications/me/read-all").header(HttpHeaders.AUTHORIZATION, auth(testSlot.schoolOwner())))
                .andExpect(status().isOk());
    }

    private Long extractId(String json) {
        int idx = json.indexOf("\"id\":");
        String rest = json.substring(idx + 5);
        int end = 0;
        while (end < rest.length() && Character.isDigit(rest.charAt(end))) {
            end++;
        }
        return Long.valueOf(rest.substring(0, end));
    }
}
