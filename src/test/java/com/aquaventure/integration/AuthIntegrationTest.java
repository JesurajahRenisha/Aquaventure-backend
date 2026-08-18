package com.aquaventure.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Transactional
    void registerThenLogin_succeeds() throws Exception {
        String registerBody = """
                {"name":"Test Tourist","email":"integration.tourist@example.com",
                 "password":"Passw0rd!","role":"SURFER"}
                """;

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("integration.tourist@example.com")))
                .andExpect(jsonPath("$.role", is("SURFER")));

        String loginBody = """
                {"email":"integration.tourist@example.com","password":"Passw0rd!"}
                """;

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role", is("SURFER")));
    }

    @Test
    @Transactional
    void register_rejectsDuplicateEmail() throws Exception {
        String registerBody = """
                {"name":"Dup Test","email":"dup.integration@example.com",
                 "password":"Passw0rd!","role":"SURFER"}
                """;

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void login_rejectsWrongPassword() throws Exception {
        String registerBody = """
                {"name":"Bad Login","email":"badlogin.integration@example.com",
                 "password":"Passw0rd!","role":"SURFER"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {"email":"badlogin.integration@example.com","password":"WrongPassw0rd!"}
                """;
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isUnauthorized());
    }
}
