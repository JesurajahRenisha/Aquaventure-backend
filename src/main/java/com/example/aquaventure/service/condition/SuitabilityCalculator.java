package com.example.aquaventure.service.condition;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.aquaventure.config.SurfSuitabilityProperties;
import com.example.aquaventure.entity.SkillLevel;

@Component
public class SuitabilityCalculator {

    static final String SUITABLE = "SUITABLE";
    static final String NOT_SUITABLE = "NOT_SUITABLE";

    private final SurfSuitabilityProperties thresholds;

    public SuitabilityCalculator(SurfSuitabilityProperties thresholds) {
        this.thresholds = thresholds;
    }

    public Map<SkillLevel, String> calculate(DailyCondition condition) {
        Map<SkillLevel, String> result = new LinkedHashMap<>();
        result.put(SkillLevel.BEGINNER, label(condition, thresholds.getBeginner()));
        result.put(SkillLevel.INTERMEDIATE, label(condition, thresholds.getIntermediate()));
        result.put(SkillLevel.ADVANCED, label(condition, thresholds.getAdvanced()));
        return result;
    }

    private String label(DailyCondition condition, SurfSuitabilityProperties.Threshold threshold) {
        boolean waveOk = condition.maxWaveHeightM() <= threshold.getMaxWaveHeightM();
        boolean windOk = condition.avgWindSpeedKmh() <= threshold.getMaxWindSpeedKmh();
        return waveOk && windOk ? SUITABLE : NOT_SUITABLE;
    }
}
