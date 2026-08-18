package com.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.BookingRequest;
import com.aquaventure.dto.BookingResponse;
import com.aquaventure.dto.PaymentRequest;
import com.aquaventure.entity.Booking;
import com.aquaventure.entity.BookingStatus;
import com.aquaventure.entity.PaymentStatus;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.Role;
import com.aquaventure.entity.SurfActivity;
import com.aquaventure.entity.Tourist;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ConflictException;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.mapper.BookingMapper;
import com.aquaventure.repository.BookingRepository;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TouristService touristService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private SurfActivityService surfActivityService;

    /**
     * 1. Validate activity exists and is available.
     * 2. Save booking with status=PENDING, payment_status=UNPAID.
     * 3. Return the booking (with id) for the payment step.
     */
    public BookingResponse create(User touristUser, BookingRequest request) {
        Tourist tourist = touristService.getEntityForUser(touristUser);
        SurfActivity activity = surfActivityService.getEntity(request.getActivityId());

        if (!activity.isActive()) {
            throw new ConflictException("This activity is not currently available for booking");
        }

        Booking booking = new Booking();
        booking.setTourist(tourist);
        booking.setActivity(activity);
        booking.setBookingDate(request.getBookingDate());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);

        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    public Booking getEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    public BookingResponse get(User requester, Long id) {
        Booking booking = getEntity(id);
        requireVisibility(requester, booking);
        return BookingMapper.toResponse(booking);
    }

    public List<BookingResponse> search(Long touristId, Long providerId, BookingStatus status) {
        List<Booking> bookings;
        if (touristId != null) {
            Tourist tourist = touristService.getEntity(touristId);
            bookings = status != null
                    ? bookingRepository.findByTouristAndStatus(tourist, status)
                    : bookingRepository.findByTourist(tourist);
        } else if (providerId != null) {
            Provider provider = providerService.getEntity(providerId);
            bookings = status != null
                    ? bookingRepository.findByActivity_ProviderAndStatus(provider, status)
                    : bookingRepository.findByActivity_Provider(provider);
        } else {
            bookings = bookingRepository.findAll();
        }
        return bookings.stream().map(BookingMapper::toResponse).toList();
    }

    /** PROVIDER confirms or cancels a booking for one of their activities. */
    public BookingResponse updateStatus(User providerUser, Long id, BookingStatus newStatus) {
        Provider provider = providerService.getEntityForUser(providerUser);
        Booking booking = getEntity(id);

        if (!booking.getActivity().getProvider().getProviderId().equals(provider.getProviderId())) {
            throw new ForbiddenActionException("This booking does not belong to your provider account");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("This booking has already been cancelled");
        }

        booking.setStatus(newStatus);
        if (newStatus == BookingStatus.CANCELLED && booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    /** Mock/stub payment gateway call: verify -> mark paid -> confirm the booking. */
    public BookingResponse pay(User touristUser, Long id, PaymentRequest request) {
        Tourist tourist = touristService.getEntityForUser(touristUser);
        Booking booking = getEntity(id);

        if (!booking.getTourist().getTouristId().equals(tourist.getTouristId())) {
            throw new ForbiddenActionException("This booking does not belong to you");
        }
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ConflictException("This booking has already been paid for");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("This booking has been cancelled and can no longer be paid for");
        }

        // Stub gateway call: in a real integration this would call out to a payment
        // provider and verify the response before marking the booking paid.
        boolean paymentVerified = request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank();
        if (!paymentVerified) {
            throw new ConflictException("Payment could not be verified");
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setStatus(BookingStatus.CONFIRMED);
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    private void requireVisibility(User requester, Booking booking) {
        boolean isTourist = booking.getTourist().getUser().getUserId().equals(requester.getUserId());
        boolean isProvider = booking.getActivity().getProvider().getUser().getUserId().equals(requester.getUserId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isTourist && !isProvider && !isAdmin) {
            throw new ForbiddenActionException("This booking does not belong to you");
        }
    }
}
