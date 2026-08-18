package com.aquaventure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.Recommendation;
import com.aquaventure.entity.Tourist;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByTouristOrderByCreatedAtDesc(Tourist tourist);
}
