package com.example.aquaventure.dto.admin;

import java.util.Map;

public class AdminStatsDto {

    private Map<String, Long> usersByRole;
    private Map<String, Long> schoolsByStatus;
    private Map<String, Long> bookingsByStatus;
    private Map<String, Long> bookingsByLocation;

    public Map<String, Long> getUsersByRole() {
        return usersByRole;
    }

    public void setUsersByRole(Map<String, Long> usersByRole) {
        this.usersByRole = usersByRole;
    }

    public Map<String, Long> getSchoolsByStatus() {
        return schoolsByStatus;
    }

    public void setSchoolsByStatus(Map<String, Long> schoolsByStatus) {
        this.schoolsByStatus = schoolsByStatus;
    }

    public Map<String, Long> getBookingsByStatus() {
        return bookingsByStatus;
    }

    public void setBookingsByStatus(Map<String, Long> bookingsByStatus) {
        this.bookingsByStatus = bookingsByStatus;
    }

    public Map<String, Long> getBookingsByLocation() {
        return bookingsByLocation;
    }

    public void setBookingsByLocation(Map<String, Long> bookingsByLocation) {
        this.bookingsByLocation = bookingsByLocation;
    }
}
