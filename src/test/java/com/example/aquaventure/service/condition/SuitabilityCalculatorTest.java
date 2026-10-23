package com.example.aquaventure.service.condition;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.aquaventure.config.SurfSuitabilityProperties;
import com.example.aquaventure.entity.SkillLevel;

class SuitabilityCalculatorTest {

    private SurfSuitabilityProperties thresholds;
    private SuitabilityCalculator calculator;

    @BeforeEach
    void setUp() {
        thresholds = new SurfSuitabilityProperties();
        thresholds.getBeginner().setMaxWaveHeightM(1.2);
        thresholds.getBeginner().setMaxWindSpeedKmh(20);
        thresholds.getIntermediate().setMaxWaveHeightM(2.0);
        thresholds.getIntermediate().setMaxWindSpeedKmh(30);
        thresholds.getAdvanced().setMaxWaveHeightM(99);
        thresholds.getAdvanced().setMaxWindSpeedKmh(99);
        calculator = new SuitabilityCalculator(thresholds);
    }

    private DailyCondition condition(double waveHeight, double windSpeed) {
        return new DailyCondition(LocalDate.of(2026, 11, 1), waveHeight, 8, 100, windSpeed, 200, 27);
    }

    @Test
    void beginner_withinThresholds_isSuitable() {
        var result = calculator.calculate(condition(1.2, 20));
        assertThat(result.get(SkillLevel.BEGINNER)).isEqualTo("SUITABLE");
    }

    @Test
    void beginner_waveHeightJustOverThreshold_isNotSuitable() {
        var result = calculator.calculate(condition(1.21, 10));
        assertThat(result.get(SkillLevel.BEGINNER)).isEqualTo("NOT_SUITABLE");
    }

    @Test
    void beginner_windSpeedJustOverThreshold_isNotSuitable() {
        var result = calculator.calculate(condition(0.5, 20.01));
        assertThat(result.get(SkillLevel.BEGINNER)).isEqualTo("NOT_SUITABLE");
    }

    @Test
    void intermediate_allowsHigherWaveAndWindThanBeginner() {
        var result = calculator.calculate(condition(1.8, 25));
        assertThat(result.get(SkillLevel.BEGINNER)).isEqualTo("NOT_SUITABLE");
        assertThat(result.get(SkillLevel.INTERMEDIATE)).isEqualTo("SUITABLE");
    }

    @Test
    void advanced_suitableInRoughConditionsThatFailOtherLevels() {
        var result = calculator.calculate(condition(5.0, 40));
        assertThat(result.get(SkillLevel.BEGINNER)).isEqualTo("NOT_SUITABLE");
        assertThat(result.get(SkillLevel.INTERMEDIATE)).isEqualTo("NOT_SUITABLE");
        assertThat(result.get(SkillLevel.ADVANCED)).isEqualTo("SUITABLE");
    }
}
