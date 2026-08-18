package com.aquaventure.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Exercises the full booking flow end to end through real HTTP requests
 * (register -> login -> create location/activity -> book -> confirm -> pay)
 * and confirms role-based access restrictions are enforced.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    void fullBookingFlow_createConfirmAndPay() throws Exception {
        String providerToken = registerAndLogin("Flow Provider", "flow.provider@example.com", "PROVIDER");
        String touristToken = registerAndLogin("Flow Tourist", "flow.tourist@example.com", "SURFER");

        // Provider creates a location and an activity.
        long locationId = extractId(mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"locationName":"Flow Point","difficultyLevel":"BEGINNER","safetyRating":5}
                                """))
                .andExpect(status().isCreated())
                .andReturn(), "locationId");

        long activityId = extractId(mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"locationId":%d,"activityName":"Flow Lesson","price":20.00,"duration":60}
                                """.formatted(locationId)))
                .andExpect(status().isCreated())
                .andReturn(), "activityId");

        // Tourist books it.
        long bookingId = extractId(mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + touristToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"activityId":%d,"bookingDate":"2027-01-01T09:00:00"}
                                """.formatted(activityId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", org.hamcrest.Matchers.is("PENDING")))
                .andExpect(jsonPath("$.paymentStatus", org.hamcrest.Matchers.is("UNPAID")))
                .andReturn(), "bookingId");

        // Provider confirms it.
        mockMvc.perform(put("/api/bookings/" + bookingId + "/status")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", org.hamcrest.Matchers.is("CONFIRMED")));

        // Tourist pays.
        mockMvc.perform(post("/api/bookings/" + bookingId + "/payment")
                        .header("Authorization", "Bearer " + touristToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paymentMethod":"card"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus", org.hamcrest.Matchers.is("PAID")));
    }

    @Test
    @Transactional
    void roleRestrictions_areEnforced() throws Exception {
        String touristToken = registerAndLogin("Restricted Tourist", "restricted.tourist@example.com", "SURFER");

        // A surfer cannot create a location (provider/admin only).
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + touristToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"locationName":"Nope","difficultyLevel":"BEGINNER","safetyRating":5}
                                """))
                .andExpect(status().isForbidden());

        // No token at all -> unauthenticated.
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndLogin(String name, String email, String role) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"Passw0rd!","role":"%s"}
                                """.formatted(name, email, role)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Passw0rd!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long extractId(MvcResult result, String fieldName) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get(fieldName).asLong();
    }
}
