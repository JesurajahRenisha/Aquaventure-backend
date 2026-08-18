package com.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaventure.dto.BookingRequest;
import com.aquaventure.dto.BookingResponse;
import com.aquaventure.dto.PaymentRequest;
import com.aquaventure.entity.Booking;
import com.aquaventure.entity.BookingStatus;
import com.aquaventure.entity.PaymentStatus;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.Role;
import com.aquaventure.entity.SurfActivity;
import com.aquaventure.entity.SurfLocation;
import com.aquaventure.entity.Tourist;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ConflictException;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TouristService touristService;

    @Mock
    private ProviderService providerService;

    @Mock
    private SurfActivityService surfActivityService;

    @InjectMocks
    private BookingService bookingService;

    private User touristUser;
    private Tourist tourist;
    private SurfActivity activity;

    @BeforeEach
    void setUp() {
        touristUser = new User();
        touristUser.setUserId(1L);
        touristUser.setRole(Role.SURFER);

        tourist = new Tourist();
        tourist.setTouristId(10L);
        tourist.setUser(touristUser);

        User providerUser = new User();
        providerUser.setUserId(2L);
        providerUser.setRole(Role.PROVIDER);

        Provider provider = new Provider();
        provider.setProviderId(20L);
        provider.setUser(providerUser);
        provider.setBusinessName("Ravi's Wave School");

        SurfLocation location = new SurfLocation();
        location.setLocationId(30L);
        location.setLocationName("Main Point");

        activity = new SurfActivity();
        activity.setActivityId(40L);
        activity.setProvider(provider);
        activity.setLocation(location);
        activity.setActivityName("Beginner Surf Lesson");
        activity.setPrice(new BigDecimal("25.00"));
        activity.setActive(true);
    }

    @Test
    void create_savesBookingAsPendingAndUnpaid() {
        when(touristService.getEntityForUser(touristUser)).thenReturn(tourist);
        when(surfActivityService.getEntity(40L)).thenReturn(activity);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingRequest request = new BookingRequest();
        request.setActivityId(40L);
        request.setBookingDate(LocalDateTime.now().plusDays(3));

        BookingResponse response = bookingService.create(touristUser, request);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(response.getPrice()).isEqualByComparingTo("25.00");
    }

    @Test
    void create_rejectsInactiveActivity() {
        activity.setActive(false);
        when(touristService.getEntityForUser(touristUser)).thenReturn(tourist);
        when(surfActivityService.getEntity(40L)).thenReturn(activity);

        BookingRequest request = new BookingRequest();
        request.setActivityId(40L);
        request.setBookingDate(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.create(touristUser, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not currently available");
    }

    @Test
    void pay_marksBookingPaidAndConfirmed() {
        Booking booking = new Booking();
        booking.setBookingId(50L);
        booking.setTourist(tourist);
        booking.setActivity(activity);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);

        when(touristService.getEntityForUser(touristUser)).thenReturn(tourist);
        when(bookingRepository.findById(50L)).thenReturn(java.util.Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod("card");

        BookingResponse response = bookingService.pay(touristUser, 50L, paymentRequest);

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void pay_rejectsWhenBookingBelongsToAnotherTourist() {
        User otherTouristUser = new User();
        otherTouristUser.setUserId(99L);
        otherTouristUser.setRole(Role.SURFER);
        Tourist otherTourist = new Tourist();
        otherTourist.setTouristId(11L);
        otherTourist.setUser(otherTouristUser);

        Booking booking = new Booking();
        booking.setBookingId(51L);
        booking.setTourist(tourist);
        booking.setActivity(activity);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);

        when(touristService.getEntityForUser(otherTouristUser)).thenReturn(otherTourist);
        when(bookingRepository.findById(51L)).thenReturn(java.util.Optional.of(booking));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod("card");

        assertThatThrownBy(() -> bookingService.pay(otherTouristUser, 51L, paymentRequest))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void updateStatus_refundsWhenCancellingAPaidBooking() {
        User providerUser = activity.getProvider().getUser();
        Booking booking = new Booking();
        booking.setBookingId(52L);
        booking.setTourist(tourist);
        booking.setActivity(activity);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);

        when(providerService.getEntityForUser(providerUser)).thenReturn(activity.getProvider());
        when(bookingRepository.findById(52L)).thenReturn(java.util.Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.updateStatus(providerUser, 52L, BookingStatus.CANCELLED);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }
}
