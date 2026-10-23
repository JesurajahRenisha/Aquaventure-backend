package com.example.aquaventure.service.condition;

import java.time.LocalDate;

public record DailyCondition(LocalDate date, double maxWaveHeightM, double avgSwellPeriodSec,
                              double dominantSwellDirectionDeg, double avgWindSpeedKmh,
                              double dominantWindDirectionDeg, double avgTemperatureC) {
}
