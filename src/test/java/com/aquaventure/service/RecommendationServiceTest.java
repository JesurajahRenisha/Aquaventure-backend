package com.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaventure.dto.RecommendationResponse;
import com.aquaventure.dto.WeatherResponse;
import com.aquaventure.entity.Recommendation;
import com.aquaventure.entity.SkillLevel;
import com.aquaventure.entity.SurfLocation;
import com.aquaventure.entity.Tourist;
import com.aquaventure.entity.User;
import com.aquaventure.repository.RecommendationRepository;
import com.aquaventure.repository.SurfLocationRepository;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private SurfLocationRepository surfLocationRepository;

    @Mock
    private TouristService touristService;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private RecommendationService recommendationService;

    private Tourist beginnerTourist;
    private SurfLocation beginnerSpot;
    private SurfLocation advancedSpot;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUserId(1L);
        user.setName("Kasun");

        beginnerTourist = new Tourist();
        beginnerTourist.setTouristId(10L);
        beginnerTourist.setUser(user);
        beginnerTourist.setSkillLevel(SkillLevel.BEGINNER);

        beginnerSpot = new SurfLocation();
        beginnerSpot.setLocationId(1L);
        beginnerSpot.setLocationName("Main Point");
        beginnerSpot.setDifficultyLevel(SkillLevel.BEGINNER);
        beginnerSpot.setSafetyRating(5);

        advancedSpot = new SurfLocation();
        advancedSpot.setLocationId(2L);
        advancedSpot.setLocationName("Whiskey Point");
        advancedSpot.setDifficultyLevel(SkillLevel.ADVANCED);
        advancedSpot.setSafetyRating(3);
    }

    @Test
    void generate_recommendsOnlyLocationsMatchingSkillLevel() {
        when(touristService.getEntity(10L)).thenReturn(beginnerTourist);
        when(surfLocationRepository.findAll()).thenReturn(List.of(beginnerSpot, advancedSpot));
        when(weatherService.getForLocation(1L))
                .thenReturn(new WeatherResponse(1L, 1L, 0.8, 10.0, 28.0, java.time.Instant.now(), true));
        when(recommendationRepository.save(any(Recommendation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<RecommendationResponse> results = recommendationService.generate(10L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLocationId()).isEqualTo(1L);
        assertThat(results.get(0).getRecommendationReason()).contains("BEGINNER");
    }

    @Test
    void generate_excludesLocationWithUnsafeWaveHeightForSkillLevel() {
        when(touristService.getEntity(10L)).thenReturn(beginnerTourist);
        when(surfLocationRepository.findAll()).thenReturn(List.of(beginnerSpot));
        // Wave height of 1.5m exceeds the 1.0m ceiling for a beginner.
        when(weatherService.getForLocation(1L))
                .thenReturn(new WeatherResponse(1L, 1L, 1.5, 20.0, 26.0, java.time.Instant.now(), true));

        List<RecommendationResponse> results = recommendationService.generate(10L);

        assertThat(results).isEmpty();
    }

    @Test
    void generate_excludesLocationBelowMinimumSafetyRating() {
        beginnerSpot.setSafetyRating(2);
        when(touristService.getEntity(10L)).thenReturn(beginnerTourist);
        when(surfLocationRepository.findAll()).thenReturn(List.of(beginnerSpot));

        List<RecommendationResponse> results = recommendationService.generate(10L);

        assertThat(results).isEmpty();
    }
}
