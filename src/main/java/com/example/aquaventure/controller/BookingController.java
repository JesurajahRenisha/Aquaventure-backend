package com.example.aquaventure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.booking.BookingDto;
import com.example.aquaventure.dto.booking.CreateBookingRequest;
import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("hasRole('SURFER')")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDto>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody CreateBookingRequest request) {
        BookingDto dto = bookingService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }
}
