package com.example.aquaventure.controller.surfer;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.surfer.RecommendationDto;
import com.example.aquaventure.dto.surfer.SurferProfileDto;
import com.example.aquaventure.dto.surfer.UpdateSurferProfileRequest;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.RecommendationService;
import com.example.aquaventure.service.SurferProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/surfer/me")
@PreAuthorize("hasRole('SURFER')")
public class SurferController {

    private final SurferProfileService surferProfileService;
    private final RecommendationService recommendationService;

    public SurferController(SurferProfileService surferProfileService, RecommendationService recommendationService) {
        this.surferProfileService = surferProfileService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/profile")
    public ApiResponse<SurferProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(surferProfileService.getOrCreateDefault(principal.getId()));
    }

    @PutMapping("/profile")
    public ApiResponse<SurferProfileDto> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody UpdateSurferProfileRequest request) {
        return ApiResponse.ok(surferProfileService.update(principal.getId(), request));
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<RecommendationDto>> recommendations(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(recommendationService.recommendFor(principal.getId()));
    }
}
