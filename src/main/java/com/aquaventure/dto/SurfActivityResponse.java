package com.aquaventure.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SurfActivityResponse {
    private Long activityId;
    private Long providerId;
    private String providerBusinessName;
    private Long locationId;
    private String locationName;
    private String activityName;
    private BigDecimal price;
    private Integer duration;
    private boolean active;
}
