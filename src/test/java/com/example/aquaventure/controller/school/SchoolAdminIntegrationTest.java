package com.example.aquaventure.controller.school;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.example.aquaventure.dto.auth.RegisterSchoolRequest;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.UserRepository;
import com.example.aquaventure.security.JwtUtil;
import com.example.aquaventure.service.AuthService;
import com.example.aquaventure.service.SurfSchoolService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SchoolAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private SurfSchoolService surfSchoolService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    private static int counter = 0;

    private User platformAdmin() {
        User admin = new User();
        admin.setFirstname("Plat");
        admin.setLastname("Admin");
        admin.setEmail("plat-admin-" + (counter++) + "@example.com");
        admin.setPassword("Hashed1!");
        admin.setRole(Role.PLATFORM_ADMIN);
        return userRepository.save(admin);
    }

    private SurfSchool registerSchool(String prefix) {
        RegisterSchoolRequest request = new RegisterSchoolRequest();
        request.setOwnerFirstname("Owner");
        request.setOwnerLastname("Test");
        request.setEmail(prefix.toLowerCase() + counter++ + "@example.com");
        request.setPassword("Password1!");
        request.setSchoolName(prefix + " Surf School");
        request.setBusinessRegistrationNumber("BRN-" + prefix + counter);
        return authService.registerSchool(request, null);
    }

    private record ApprovedSchool(String token, Long schoolId) {
    }

    private ApprovedSchool approvedSchool(String prefix) {
        SurfSchool school = registerSchool(prefix);
        surfSchoolService.approve(school.getId(), platformAdmin());
        return new ApprovedSchool(jwtUtil.generateToken(school.getOwner()), school.getId());
    }

    private String auth(String token) {
        return "Bearer " + token;
    }

    // --- PENDING school cannot manage sub-resources ---

    @Test
    void pendingSchool_createInstructor_returns403() throws Exception {
        SurfSchool pending = registerSchool("Pending");
        String token = jwtUtil.generateToken(pending.getOwner());

        mockMvc.perform(post("/api/school/me/instructors")
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Instructor One\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("SCHOOL_NOT_APPROVED")));
    }

    // --- profile editable while PENDING, blocked while SUSPENDED ---

    @Test
    void pendingSchool_canViewAndEditProfile() throws Exception {
        SurfSchool pending = registerSchool("PendingProfile");
        String token = jwtUtil.generateToken(pending.getOwner());

        mockMvc.perform(get("/api/school/me").header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/school/me")
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Updated Name")));
    }

    @Test
    void suspendedSchool_cannotEditProfile() throws Exception {
        ApprovedSchool school = approvedSchool("Suspend");
        surfSchoolService.suspend(school.schoolId());

        mockMvc.perform(put("/api/school/me")
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Should Not Save\"}"))
                .andExpect(status().isForbidden());
    }

    // --- cross-school ownership isolation ---

    @Test
    void crossSchool_instructorOwnershipViolation_returns403() throws Exception {
        ApprovedSchool schoolA = approvedSchool("OwnA1");
        ApprovedSchool schoolB = approvedSchool("OwnB1");

        String createBody = "{\"name\":\"Instructor A\"}";
        String location = mockMvc.perform(post("/api/school/me/instructors")
                        .header(HttpHeaders.AUTHORIZATION, auth(schoolA.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long instructorId = extractId(location);

        mockMvc.perform(put("/api/school/me/instructors/" + instructorId)
                        .header(HttpHeaders.AUTHORIZATION, auth(schoolB.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hijacked\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN_OWNERSHIP")));

        mockMvc.perform(delete("/api/school/me/instructors/" + instructorId)
                        .header(HttpHeaders.AUTHORIZATION, auth(schoolB.token())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crossSchool_lessonOwnershipViolation_returns403() throws Exception {
        ApprovedSchool schoolA = approvedSchool("OwnA2");
        ApprovedSchool schoolB = approvedSchool("OwnB2");

        Long lessonId = createLesson(schoolA.token());

        mockMvc.perform(put("/api/school/me/lessons/" + lessonId)
                        .header(HttpHeaders.AUTHORIZATION, auth(schoolB.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lessonJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN_OWNERSHIP")));
    }

    @Test
    void crossSchool_equipmentOwnershipViolation_returns403() throws Exception {
        ApprovedSchool schoolA = approvedSchool("OwnA3");
        ApprovedSchool schoolB = approvedSchool("OwnB3");

        String response = mockMvc.perform(post("/api/school/me/equipment")
                        .header(HttpHeaders.AUTHORIZATION, auth(schoolA.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(equipmentJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long equipmentId = extractId(response);

        mockMvc.perform(delete("/api/school/me/equipment/" + equipmentId)
                        .header(HttpHeaders.AUTHORIZATION, auth(schoolB.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN_OWNERSHIP")));
    }

    @Test
    void crossSchool_slotOwnershipViolation_returns403() throws Exception {
        ApprovedSchool schoolA = approvedSchool("OwnA4");
        ApprovedSchool schoolB = approvedSchool("OwnB4");

        Long lessonId = createLesson(schoolA.token());
        Long instructorId = createInstructor(schoolA.token());
        Long slotId = createSlot(schoolA.token(), lessonId, instructorId, Instant.parse("2026-11-05T08:00:00Z"));

        mockMvc.perform(delete("/api/school/me/slots/" + slotId)
                        .header(HttpHeaders.AUTHORIZATION, auth(schoolB.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN_OWNERSHIP")));
    }

    // --- CRUD happy paths ---

    @Test
    void instructor_crudHappyPath() throws Exception {
        ApprovedSchool school = approvedSchool("CrudInstr");

        String response = mockMvc.perform(post("/api/school/me/instructors")
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jane Surf\",\"experienceYears\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Jane Surf")))
                .andReturn().getResponse().getContentAsString();
        Long id = extractId(response);

        mockMvc.perform(put("/api/school/me/instructors/" + id)
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jane Updated\",\"experienceYears\":6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Jane Updated")));

        mockMvc.perform(get("/api/school/me/instructors").header(HttpHeaders.AUTHORIZATION, auth(school.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(1)));

        mockMvc.perform(delete("/api/school/me/instructors/" + id).header(HttpHeaders.AUTHORIZATION, auth(school.token())))
                .andExpect(status().isOk());
    }

    @Test
    void lesson_crudHappyPath() throws Exception {
        ApprovedSchool school = approvedSchool("CrudLesson");

        String response = mockMvc.perform(post("/api/school/me/lessons")
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lessonJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = extractId(response);

        mockMvc.perform(put("/api/school/me/lessons/" + id)
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lessonJson().replace("Beginner Group", "Updated Group")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Updated Group Lesson")));

        mockMvc.perform(delete("/api/school/me/lessons/" + id).header(HttpHeaders.AUTHORIZATION, auth(school.token())))
                .andExpect(status().isOk());
    }

    @Test
    void equipment_crudHappyPath() throws Exception {
        ApprovedSchool school = approvedSchool("CrudEquip");

        String response = mockMvc.perform(post("/api/school/me/equipment")
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(equipmentJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = extractId(response);

        mockMvc.perform(put("/api/school/me/equipment/" + id)
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(equipmentJson().replace("9.5\"", "8.0\"")))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/school/me/equipment/" + id).header(HttpHeaders.AUTHORIZATION, auth(school.token())))
                .andExpect(status().isOk());
    }

    @Test
    void slot_crudHappyPathAndOverlapRejected() throws Exception {
        ApprovedSchool school = approvedSchool("CrudSlot");
        Long lessonId = createLesson(school.token());
        Long instructorId = createInstructor(school.token());

        Instant start = Instant.parse("2026-11-10T09:00:00Z");
        String slotBody = slotJson(lessonId, instructorId, start, start.plus(90, ChronoUnit.MINUTES));

        String response = mockMvc.perform(post("/api/school/me/slots")
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(slotBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long slotId = extractId(response);

        // overlapping slot for the same instructor -> 409
        mockMvc.perform(post("/api/school/me/slots")
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(slotJson(lessonId, instructorId,
                                start.plus(30, ChronoUnit.MINUTES), start.plus(100, ChronoUnit.MINUTES))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code", is("SLOT_OVERLAP")));

        // back-to-back slot -> allowed
        mockMvc.perform(post("/api/school/me/slots")
                        .header(HttpHeaders.AUTHORIZATION, auth(school.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(slotJson(lessonId, instructorId,
                                start.plus(90, ChronoUnit.MINUTES), start.plus(180, ChronoUnit.MINUTES))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/school/me/slots/" + slotId).header(HttpHeaders.AUTHORIZATION, auth(school.token())))
                .andExpect(status().isOk());
    }

    // --- helpers ---

    private Long createInstructor(String token) throws Exception {
        String response = mockMvc.perform(post("/api/school/me/instructors")
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Helper Instructor\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractId(response);
    }

    private Long createLesson(String token) throws Exception {
        String response = mockMvc.perform(post("/api/school/me/lessons")
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lessonJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractId(response);
    }

    private Long createSlot(String token, Long lessonId, Long instructorId, Instant start) throws Exception {
        String response = mockMvc.perform(post("/api/school/me/slots")
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(slotJson(lessonId, instructorId, start, start.plus(60, ChronoUnit.MINUTES))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractId(response);
    }

    private String lessonJson() {
        return "{\"title\":\"Beginner Group Lesson\",\"lessonType\":\"GROUP\",\"skillLevel\":\"BEGINNER\","
                + "\"durationMinutes\":90,\"price\":50.00,\"currency\":\"USD\",\"maxParticipants\":6}";
    }

    private String equipmentJson() {
        return "{\"type\":\"SURFBOARD\",\"name\":\"Foam Board 9.5\\\"\",\"totalQuantity\":10,"
                + "\"rentalPricePerHour\":5.00,\"rentalPricePerDay\":20.00}";
    }

    private String slotJson(Long lessonId, Long instructorId, Instant start, Instant end) {
        return "{\"lessonId\":" + lessonId + ",\"instructorId\":" + instructorId
                + ",\"startTime\":\"" + start + "\",\"endTime\":\"" + end + "\",\"capacity\":5}";
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
