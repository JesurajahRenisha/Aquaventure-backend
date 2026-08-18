package com.aquaventure.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WeatherResponse {
    private Long weatherId;
    private Long locationId;
    private Double waveHeight;
    private Double windSpeed;
    private Double temperature;
    private Instant recordedAt;
    private boolean fromCache;
}
