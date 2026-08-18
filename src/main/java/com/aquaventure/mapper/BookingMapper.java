package com.aquaventure.mapper;

import com.aquaventure.dto.BookingResponse;
import com.aquaventure.entity.Booking;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getBookingId(),
                booking.getTourist().getTouristId(),
                booking.getTourist().getUser().getName(),
                booking.getActivity().getActivityId(),
                booking.getActivity().getActivityName(),
                booking.getActivity().getProvider().getProviderId(),
                booking.getActivity().getProvider().getBusinessName(),
                booking.getActivity().getPrice(),
                booking.getBookingDate(),
                booking.getStatus(),
                booking.getPaymentStatus(),
                booking.getCreatedAt());
    }
}
