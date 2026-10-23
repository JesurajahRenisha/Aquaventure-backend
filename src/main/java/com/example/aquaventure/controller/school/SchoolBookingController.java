package com.example.aquaventure.controller.school;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.booking.BookingDto;
import com.example.aquaventure.dto.booking.RespondBookingRequest;
import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.common.PageResponse;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/school/me/bookings")
@PreAuthorize("hasRole('SCHOOL_ADMIN')")
public class SchoolBookingController {

    private final BookingService bookingService;

    public SchoolBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ApiResponse<PageResponse<BookingDto>> list(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestParam(required = false) BookingStatus status,
                                                        Pageable pageable) {
        Page<BookingDto> page = bookingService.listForSchool(principal.getId(), status, pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @PatchMapping("/{id}/accept")
    public ApiResponse<BookingDto> accept(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.ok(bookingService.accept(principal.getId(), id));
    }

    @PatchMapping("/{id}/reject")
    public ApiResponse<BookingDto> reject(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                           @Valid @RequestBody RespondBookingRequest request) {
        return ApiResponse.ok(bookingService.reject(principal.getId(), id, request.getNote()));
    }

    @PatchMapping("/{id}/complete")
    public ApiResponse<BookingDto> complete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.ok(bookingService.complete(principal.getId(), id));
    }
}
