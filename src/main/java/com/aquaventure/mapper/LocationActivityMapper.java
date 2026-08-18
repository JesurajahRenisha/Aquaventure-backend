package com.aquaventure.mapper;

import com.aquaventure.dto.SurfActivityResponse;
import com.aquaventure.dto.SurfLocationResponse;
import com.aquaventure.entity.SurfActivity;
import com.aquaventure.entity.SurfLocation;

public final class LocationActivityMapper {

    private LocationActivityMapper() {
    }

    public static SurfLocationResponse toResponse(SurfLocation location) {
        return new SurfLocationResponse(
                location.getLocationId(),
                location.getLocationName(),
                location.getDifficultyLevel(),
                location.getSafetyRating());
    }

    public static SurfActivityResponse toResponse(SurfActivity activity) {
        return new SurfActivityResponse(
                activity.getActivityId(),
                activity.getProvider().getProviderId(),
                activity.getProvider().getBusinessName(),
                activity.getLocation().getLocationId(),
                activity.getLocation().getLocationName(),
                activity.getActivityName(),
                activity.getPrice(),
                activity.getDuration(),
                activity.isActive());
    }
}
