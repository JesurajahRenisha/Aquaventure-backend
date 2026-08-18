package com.aquaventure.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.aquaventure.entity.BookingStatus;
import com.aquaventure.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private Long touristId;
    private String touristName;
    private Long activityId;
    private String activityName;
    private Long providerId;
    private String providerBusinessName;
    private BigDecimal price;
    private LocalDateTime bookingDate;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private Instant createdAt;
}
