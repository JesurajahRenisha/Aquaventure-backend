package com.example.aquaventure.dto.location;

public class LocationDto {

    private Long id;
    private String name;
    private String region;
    private String description;
    private Double latitude;
    private Double longitude;
    private boolean active;

    public LocationDto() {
    }

    public LocationDto(Long id, String name, String region, String description,
                        Double latitude, Double longitude, boolean active) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
