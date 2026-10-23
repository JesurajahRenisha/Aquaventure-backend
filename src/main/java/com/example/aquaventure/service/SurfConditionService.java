package com.example.aquaventure.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.location.DailyConditionDto;
import com.example.aquaventure.dto.location.SurfConditionDto;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.service.condition.DailyCondition;
import com.example.aquaventure.service.condition.SuitabilityCalculator;
import com.example.aquaventure.service.condition.SurfConditionProvider;

@Service
public class SurfConditionService {

    private final SurfLocationService surfLocationService;
    private final SurfConditionProvider surfConditionProvider;
    private final SuitabilityCalculator suitabilityCalculator;

    public SurfConditionService(SurfLocationService surfLocationService, SurfConditionProvider surfConditionProvider,
                                 SuitabilityCalculator suitabilityCalculator) {
        this.surfLocationService = surfLocationService;
        this.surfConditionProvider = surfConditionProvider;
        this.suitabilityCalculator = suitabilityCalculator;
    }

    @Transactional(readOnly = true)
    public SurfConditionDto getConditions(Long locationId) {
        SurfLocation location = surfLocationService.getByIdOrThrow(locationId);

        SurfConditionDto dto = new SurfConditionDto();
        dto.setLocationId(location.getId());
        dto.setLocationName(location.getName());
        dto.setDays(surfConditionProvider.getConditions(location.getLatitude(), location.getLongitude())
                .stream().map(this::toDayDto).toList());
        return dto;
    }

    private DailyConditionDto toDayDto(DailyCondition condition) {
        DailyConditionDto dto = new DailyConditionDto();
        dto.setDate(condition.date());
        dto.setMaxWaveHeightM(condition.maxWaveHeightM());
        dto.setAvgSwellPeriodSec(condition.avgSwellPeriodSec());
        dto.setDominantSwellDirectionDeg(condition.dominantSwellDirectionDeg());
        dto.setAvgWindSpeedKmh(condition.avgWindSpeedKmh());
        dto.setDominantWindDirectionDeg(condition.dominantWindDirectionDeg());
        dto.setAvgTemperatureC(condition.avgTemperatureC());
        dto.setSuitability(suitabilityCalculator.calculate(condition));
        return dto;
    }
}
