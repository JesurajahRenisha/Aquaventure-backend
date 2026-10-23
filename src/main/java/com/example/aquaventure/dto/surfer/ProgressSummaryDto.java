package com.example.aquaventure.dto.surfer;

import java.util.List;
import java.util.Map;

import com.example.aquaventure.entity.SkillLevel;

public class ProgressSummaryDto {

    private long totalLessons;
    private double totalHoursInWater;
    private Map<String, Long> sessionsPerLocation;
    private List<RatingPoint> ratingTrend;
    private SkillLevel currentSkillLevel;

    public long getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(long totalLessons) {
        this.totalLessons = totalLessons;
    }

    public double getTotalHoursInWater() {
        return totalHoursInWater;
    }

    public void setTotalHoursInWater(double totalHoursInWater) {
        this.totalHoursInWater = totalHoursInWater;
    }

    public Map<String, Long> getSessionsPerLocation() {
        return sessionsPerLocation;
    }

    public void setSessionsPerLocation(Map<String, Long> sessionsPerLocation) {
        this.sessionsPerLocation = sessionsPerLocation;
    }

    public List<RatingPoint> getRatingTrend() {
        return ratingTrend;
    }

    public void setRatingTrend(List<RatingPoint> ratingTrend) {
        this.ratingTrend = ratingTrend;
    }

    public SkillLevel getCurrentSkillLevel() {
        return currentSkillLevel;
    }

    public void setCurrentSkillLevel(SkillLevel currentSkillLevel) {
        this.currentSkillLevel = currentSkillLevel;
    }

    public record RatingPoint(java.time.Instant date, Integer rating) {
    }
}
