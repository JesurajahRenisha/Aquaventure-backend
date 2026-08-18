package com.aquaventure.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {

    @NotNull(message = "Activity is required")
    private Long activityId;

    @NotNull(message = "Booking date is required")
    private LocalDateTime bookingDate;
}
