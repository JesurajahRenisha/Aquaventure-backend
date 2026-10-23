package com.example.aquaventure.dto.school;

import java.math.BigDecimal;

public class RevenueSummaryDto {

    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private long completedBookingsCount;

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public void setRevenueThisMonth(BigDecimal revenueThisMonth) {
        this.revenueThisMonth = revenueThisMonth;
    }

    public long getCompletedBookingsCount() {
        return completedBookingsCount;
    }

    public void setCompletedBookingsCount(long completedBookingsCount) {
        this.completedBookingsCount = completedBookingsCount;
    }
}
