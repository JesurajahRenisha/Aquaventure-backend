package com.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.RecommendationResponse;
import com.aquaventure.dto.WeatherResponse;
import com.aquaventure.entity.Recommendation;
import com.aquaventure.entity.SkillLevel;
import com.aquaventure.entity.SurfLocation;
import com.aquaventure.entity.Tourist;
import com.aquaventure.repository.RecommendationRepository;
import com.aquaventure.repository.SurfLocationRepository;

/**
 * Matches a tourist's skill level against each surf location's difficulty
 * level, safety rating, and latest weather (wave height) to decide which
 * locations are currently suitable, then persists one Recommendation row per
 * suitable location.
 */
@Service
@Transactional
public class RecommendationService {

    private static final int MIN_SAFETY_RATING = 3;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private SurfLocationRepository surfLocationRepository;

    @Autowired
    private TouristService touristService;

    @Autowired
    private WeatherService weatherService;

    public List<RecommendationResponse> listForTourist(Long touristId) {
        Tourist tourist = touristService.getEntity(touristId);
        return recommendationRepository.findByTouristOrderByCreatedAtDesc(tourist).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RecommendationResponse> generate(Long touristId) {
        Tourist tourist = touristService.getEntity(touristId);
        SkillLevel skillLevel = tourist.getSkillLevel() != null ? tourist.getSkillLevel() : SkillLevel.BEGINNER;

        List<Recommendation> created = surfLocationRepository.findAll().stream()
                .map(location -> evaluate(tourist, skillLevel, location))
                .filter(java.util.Objects::nonNull)
                .map(recommendationRepository::save)
                .toList();

        return created.stream().map(this::toResponse).toList();
    }

    private Recommendation evaluate(Tourist tourist, SkillLevel touristSkill, SurfLocation location) {
        if (location.getDifficultyLevel() != touristSkill) {
            return null;
        }
        if (location.getSafetyRating() != null && location.getSafetyRating() < MIN_SAFETY_RATING) {
            return null;
        }

        WeatherResponse weather = weatherService.getForLocation(location.getLocationId());
        double maxWaveHeightForSkill = switch (touristSkill) {
            case BEGINNER -> 1.0;
            case INTERMEDIATE -> 2.0;
            case ADVANCED -> Double.MAX_VALUE;
        };
        if (weather.getWaveHeight() != null && weather.getWaveHeight() > maxWaveHeightForSkill) {
            return null;
        }

        Recommendation recommendation = new Recommendation();
        recommendation.setTourist(tourist);
        recommendation.setLocation(location);
        recommendation.setRecommendationReason(String.format(
                "Matches your %s skill level, safety rating %s/5, current wave height %.1fm",
                touristSkill, location.getSafetyRating(),
                weather.getWaveHeight() != null ? weather.getWaveHeight() : 0.0));
        return recommendation;
    }

    private RecommendationResponse toResponse(Recommendation recommendation) {
        return new RecommendationResponse(
                recommendation.getRecommendationId(),
                recommendation.getTourist().getTouristId(),
                recommendation.getLocation().getLocationId(),
                recommendation.getLocation().getLocationName(),
                recommendation.getRecommendationReason(),
                recommendation.getCreatedAt());
    }
}
