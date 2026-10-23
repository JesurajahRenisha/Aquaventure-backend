package com.example.aquaventure.controller.discovery;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.aquaventure.dto.auth.RegisterSchoolRequest;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.UserRepository;
import com.example.aquaventure.service.AuthService;
import com.example.aquaventure.service.SurfSchoolService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SchoolDiscoveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private SurfSchoolService surfSchoolService;
    @Autowired
    private UserRepository userRepository;

    private static int counter = 0;

    private Long approvedSchool(String prefix) {
        RegisterSchoolRequest request = new RegisterSchoolRequest();
        request.setOwnerFirstname("Owner");
        request.setOwnerLastname("Test");
        request.setEmail("disc-" + prefix.toLowerCase() + (counter++) + "@example.com");
        request.setPassword("Password1!");
        request.setSchoolName(prefix + " Discovery School");
        request.setBusinessRegistrationNumber("BRN-DISC-" + counter);
        SurfSchool school = authService.registerSchool(request, null);

        User admin = new User();
        admin.setFirstname("Plat");
        admin.setLastname("Admin");
        admin.setEmail("disc-admin-" + (counter++) + "@example.com");
        admin.setPassword("Hashed1!");
        admin.setRole(Role.PLATFORM_ADMIN);
        admin = userRepository.save(admin);

        surfSchoolService.approve(school.getId(), admin);
        return school.getId();
    }

    @Test
    void listSchools_noToken_returns200() throws Exception {
        approvedSchool("ListNoToken");

        mockMvc.perform(get("/api/schools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void getSchoolDetail_noToken_returns200() throws Exception {
        Long id = approvedSchool("DetailNoToken");

        mockMvc.perform(get("/api/schools/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(id.intValue())));
    }

    @Test
    void compare_tooFewIds_returns400() throws Exception {
        mockMvc.perform(get("/api/schools/compare").param("ids", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void compare_tooManyIds_returns400() throws Exception {
        mockMvc.perform(get("/api/schools/compare").param("ids", "1", "2", "3", "4", "5", "6"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void compare_validRange_returns200() throws Exception {
        Long id1 = approvedSchool("CompareA");
        Long id2 = approvedSchool("CompareB");

        mockMvc.perform(get("/api/schools/compare").param("ids", id1.toString(), id2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void listSchools_sortedByName_isAlphabetical() throws Exception {
        approvedSchool("Zebra");
        approvedSchool("Alpha");

        mockMvc.perform(get("/api/schools").param("sort", "name").param("size", "100"))
                .andExpect(status().isOk());
    }
}
