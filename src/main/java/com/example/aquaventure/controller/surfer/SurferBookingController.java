package com.example.aquaventure.controller.surfer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.booking.BookingDto;
import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.common.PageResponse;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.BookingService;

@RestController
@RequestMapping("/api/surfer/me/bookings")
@PreAuthorize("hasRole('SURFER')")
public class SurferBookingController {

    private final BookingService bookingService;

    public SurferBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ApiResponse<PageResponse<BookingDto>> list(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestParam(required = false) BookingStatus status,
                                                        Pageable pageable) {
        Page<BookingDto> page = bookingService.listForSurfer(principal.getId(), status, pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<BookingDto> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.ok(bookingService.cancel(principal.getId(), id));
    }
}
