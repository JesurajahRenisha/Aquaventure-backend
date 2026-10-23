package com.example.aquaventure.controller.discovery;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.aquaventure.repository.SurfLocationRepository;
import com.example.aquaventure.service.condition.DailyCondition;
import com.example.aquaventure.service.condition.SurfConditionProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConditionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SurfLocationRepository surfLocationRepository;

    @MockitoBean
    private SurfConditionProvider surfConditionProvider;

    @Test
    void getConditions_unknownLocation_returns404() throws Exception {
        mockMvc.perform(get("/api/locations/999999/conditions"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getConditions_knownLocation_returns200WithSuitability() throws Exception {
        var location = surfLocationRepository.findAll().stream().findFirst().orElseGet(() -> {
            var loc = new com.example.aquaventure.entity.SurfLocation();
            loc.setName("Condition Test Spot");
            loc.setLatitude(6.84);
            loc.setLongitude(81.83);
            return surfLocationRepository.save(loc);
        });

        when(surfConditionProvider.getConditions(location.getLatitude(), location.getLongitude()))
                .thenReturn(List.of(new DailyCondition(LocalDate.of(2026, 11, 1), 1.0, 8, 100, 15, 200, 27)));

        mockMvc.perform(get("/api/locations/" + location.getId() + "/conditions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.days[0].suitability.BEGINNER", is("SUITABLE")));
    }
}
