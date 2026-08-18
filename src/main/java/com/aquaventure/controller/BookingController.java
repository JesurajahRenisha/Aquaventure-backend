package com.aquaventure.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.BookingRequest;
import com.aquaventure.dto.BookingResponse;
import com.aquaventure.dto.PaymentRequest;
import com.aquaventure.dto.UpdateBookingStatusRequest;
import com.aquaventure.entity.BookingStatus;
import com.aquaventure.security.UserPrincipal;
import com.aquaventure.service.BookingService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('SURFER')")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BookingRequest request) {
        return bookingService.create(principal.getUser(), request);
    }

    @GetMapping
    public List<BookingResponse> search(@RequestParam(required = false) Long touristId,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) BookingStatus status) {
        return bookingService.search(touristId, providerId, status);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return bookingService.get(principal.getUser(), id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PROVIDER')")
    public BookingResponse updateStatus(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        return bookingService.updateStatus(principal.getUser(), id, request.getStatus());
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("hasRole('SURFER')")
    public BookingResponse pay(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        return bookingService.pay(principal.getUser(), id, request);
    }
}
