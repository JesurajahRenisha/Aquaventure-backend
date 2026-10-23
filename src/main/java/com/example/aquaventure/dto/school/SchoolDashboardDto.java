package com.example.aquaventure.dto.school;

import java.util.List;

public class SchoolDashboardDto {

    private long pendingBookingsCount;
    private List<LessonSlotDto> upcomingLessons;
    private RevenueSummaryDto revenue;

    public long getPendingBookingsCount() {
        return pendingBookingsCount;
    }

    public void setPendingBookingsCount(long pendingBookingsCount) {
        this.pendingBookingsCount = pendingBookingsCount;
    }

    public List<LessonSlotDto> getUpcomingLessons() {
        return upcomingLessons;
    }

    public void setUpcomingLessons(List<LessonSlotDto> upcomingLessons) {
        this.upcomingLessons = upcomingLessons;
    }

    public RevenueSummaryDto getRevenue() {
        return revenue;
    }

    public void setRevenue(RevenueSummaryDto revenue) {
        this.revenue = revenue;
    }
}
