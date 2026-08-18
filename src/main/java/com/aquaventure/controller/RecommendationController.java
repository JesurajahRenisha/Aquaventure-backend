package com.aquaventure.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.RecommendationResponse;
import com.aquaventure.service.RecommendationService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/tourist/{touristId}")
    public List<RecommendationResponse> listForTourist(@PathVariable Long touristId) {
        return recommendationService.listForTourist(touristId);
    }

    @PostMapping("/generate/{touristId}")
    public List<RecommendationResponse> generate(@PathVariable Long touristId) {
        return recommendationService.generate(touristId);
    }
}
