package com.example.aquaventure.dto.location;

import java.time.LocalDate;
import java.util.Map;

import com.example.aquaventure.entity.SkillLevel;

public class DailyConditionDto {

    private LocalDate date;
    private double maxWaveHeightM;
    private double avgSwellPeriodSec;
    private double dominantSwellDirectionDeg;
    private double avgWindSpeedKmh;
    private double dominantWindDirectionDeg;
    private double avgTemperatureC;
    private Map<SkillLevel, String> suitability;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getMaxWaveHeightM() {
        return maxWaveHeightM;
    }

    public void setMaxWaveHeightM(double maxWaveHeightM) {
        this.maxWaveHeightM = maxWaveHeightM;
    }

    public double getAvgSwellPeriodSec() {
        return avgSwellPeriodSec;
    }

    public void setAvgSwellPeriodSec(double avgSwellPeriodSec) {
        this.avgSwellPeriodSec = avgSwellPeriodSec;
    }

    public double getDominantSwellDirectionDeg() {
        return dominantSwellDirectionDeg;
    }

    public void setDominantSwellDirectionDeg(double dominantSwellDirectionDeg) {
        this.dominantSwellDirectionDeg = dominantSwellDirectionDeg;
    }

    public double getAvgWindSpeedKmh() {
        return avgWindSpeedKmh;
    }

    public void setAvgWindSpeedKmh(double avgWindSpeedKmh) {
        this.avgWindSpeedKmh = avgWindSpeedKmh;
    }

    public double getDominantWindDirectionDeg() {
        return dominantWindDirectionDeg;
    }

    public void setDominantWindDirectionDeg(double dominantWindDirectionDeg) {
        this.dominantWindDirectionDeg = dominantWindDirectionDeg;
    }

    public double getAvgTemperatureC() {
        return avgTemperatureC;
    }

    public void setAvgTemperatureC(double avgTemperatureC) {
        this.avgTemperatureC = avgTemperatureC;
    }

    public Map<SkillLevel, String> getSuitability() {
        return suitability;
    }

    public void setSuitability(Map<SkillLevel, String> suitability) {
        this.suitability = suitability;
    }
}
