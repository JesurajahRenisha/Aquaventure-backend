package com.example.aquaventure.mapper;

import com.example.aquaventure.dto.location.LocationDto;
import com.example.aquaventure.entity.SurfLocation;

public final class SurfLocationMapper {

    private SurfLocationMapper() {
    }

    public static LocationDto toDto(SurfLocation location) {
        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getRegion(),
                location.getDescription(),
                location.getLatitude(),
                location.getLongitude(),
                location.isActive());
    }
}
