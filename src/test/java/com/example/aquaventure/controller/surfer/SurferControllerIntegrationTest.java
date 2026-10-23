package com.example.aquaventure.controller.surfer;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.UserRepository;
import com.example.aquaventure.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SurferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void updateProfile_spoofedIdentityFieldInBody_isIgnored_profileResolvedFromToken() throws Exception {
        User surfer = new User();
        surfer.setFirstname("Real");
        surfer.setLastname("Surfer");
        surfer.setEmail("real-surfer@example.com");
        surfer.setPassword(passwordEncoder.encode("Password1!"));
        surfer.setRole(Role.SURFER);
        surfer = userRepository.save(surfer);
        String token = jwtUtil.generateToken(surfer);

        // "userId" isn't even a field on UpdateSurferProfileRequest - this proves a client can't
        // influence whose profile gets updated by injecting extra identity-looking fields.
        String body = "{\"skillLevel\":\"INTERMEDIATE\",\"userId\":999999,\"bio\":\"Nice waves\"}";

        mockMvc.perform(put("/api/surfer/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skillLevel", is("INTERMEDIATE")))
                .andExpect(jsonPath("$.data.bio", is("Nice waves")));
    }
}
