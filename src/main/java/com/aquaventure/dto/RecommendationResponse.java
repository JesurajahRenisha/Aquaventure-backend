package com.aquaventure.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RecommendationResponse {
    private Long recommendationId;
    private Long touristId;
    private Long locationId;
    private String locationName;
    private String recommendationReason;
    private Instant createdAt;
}
