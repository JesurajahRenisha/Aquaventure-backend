package com.example.aquaventure.dto.location;

import java.util.List;

public class SurfConditionDto {

    private Long locationId;
    private String locationName;
    private List<DailyConditionDto> days;

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public List<DailyConditionDto> getDays() {
        return days;
    }

    public void setDays(List<DailyConditionDto> days) {
        this.days = days;
    }
}
