package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.aquaventure.dto.booking.BookingDto;
import com.example.aquaventure.dto.booking.CreateBookingRequest;
import com.example.aquaventure.dto.booking.EquipmentLineItemRequest;
import com.example.aquaventure.entity.Booking;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.Equipment;
import com.example.aquaventure.entity.EquipmentType;
import com.example.aquaventure.entity.Instructor;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonSlotStatus;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.RentalUnit;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.ConflictException;
import com.example.aquaventure.exception.InvalidStateTransitionException;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.SchoolNotApprovedException;
import com.example.aquaventure.repository.BookingEquipmentItemRepository;
import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.EquipmentRepository;
import com.example.aquaventure.repository.LessonSlotRepository;
import com.example.aquaventure.repository.UserRepository;

class BookingServiceTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final BookingEquipmentItemRepository bookingEquipmentItemRepository = mock(BookingEquipmentItemRepository.class);
    private final LessonSlotRepository lessonSlotRepository = mock(LessonSlotRepository.class);
    private final EquipmentRepository equipmentRepository = mock(EquipmentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SurfSchoolService surfSchoolService = mock(SurfSchoolService.class);
    private final LessonSlotCapacityService lessonSlotCapacityService = mock(LessonSlotCapacityService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private BookingService bookingService;

    private SurfSchool school;
    private User owner;
    private User surfer;
    private LessonSlot slot;
    private Instructor instructor;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, bookingEquipmentItemRepository, lessonSlotRepository,
                equipmentRepository, userRepository, surfSchoolService, lessonSlotCapacityService,
                notificationService, eventPublisher);

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        school = new SurfSchool();
        school.setId(10L);
        school.setOwner(owner);
        school.setStatus(SchoolStatus.APPROVED);

        instructor = new Instructor();
        instructor.setId(20L);
        instructor.setSchool(school);
        instructor.setName("Instructor");

        lesson = new Lesson();
        lesson.setId(30L);
        lesson.setSchool(school);
        lesson.setTitle("Beginner Lesson");
        lesson.setLessonType(LessonType.GROUP);
        lesson.setSkillLevel(SkillLevel.BEGINNER);
        lesson.setDurationMinutes(60);
        lesson.setPrice(BigDecimal.valueOf(40));
        lesson.setCurrency(Currency.USD);
        lesson.setMaxParticipants(5);

        slot = new LessonSlot();
        slot.setId(40L);
        slot.setLesson(lesson);
        slot.setInstructor(instructor);
        slot.setStartTime(Instant.now().plus(2, ChronoUnit.DAYS));
        slot.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS).plus(90, ChronoUnit.MINUTES));
        slot.setCapacity(5);
        slot.setBookedCount(0);
        slot.setStatus(LessonSlotStatus.OPEN);

        surfer = new User();
        surfer.setId(50L);
        surfer.setFirstname("Surfer");
        surfer.setEmail("surfer@example.com");

        when(lessonSlotRepository.findById(40L)).thenReturn(Optional.of(slot));
        when(userRepository.findById(50L)).thenReturn(Optional.of(surfer));
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) {
                b.setId(100L);
            }
            return b;
        });
        when(bookingEquipmentItemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookingEquipmentItemRepository.findByBookingId(any())).thenReturn(List.of());
        when(surfSchoolService.getOwnedSchoolOrThrow(1L)).thenReturn(school);
    }

    private CreateBookingRequest request(int participants) {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setLessonSlotId(40L);
        req.setParticipants(participants);
        return req;
    }

    @Test
    void create_withoutEquipment_computesPriceFromLessonOnly() {
        BookingDto dto = bookingService.create(50L, request(2));

        assertThat(dto.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(notificationService).notify(org.mockito.ArgumentMatchers.eq(owner),
                org.mockito.ArgumentMatchers.eq(com.example.aquaventure.entity.NotificationType.BOOKING_CREATED), any());
    }

    @Test
    void create_withEquipment_addsLineTotalsToPrice() {
        Equipment board = new Equipment();
        board.setId(60L);
        board.setSchool(school);
        board.setType(EquipmentType.SURFBOARD);
        board.setName("Foam board");
        board.setTotalQuantity(5);
        board.setAvailable(true);
        board.setRentalPricePerHour(BigDecimal.valueOf(5));
        board.setRentalPricePerDay(BigDecimal.valueOf(20));
        when(equipmentRepository.findById(60L)).thenReturn(Optional.of(board));

        EquipmentLineItemRequest line = new EquipmentLineItemRequest();
        line.setEquipmentId(60L);
        line.setQuantity(2);
        line.setRentalUnit(RentalUnit.HOUR);

        CreateBookingRequest req = request(1);
        req.setEquipmentItems(List.of(line));

        BookingDto dto = bookingService.create(50L, req);

        // 40 (lesson) + 2 * 5 (equipment) = 50
        assertThat(dto.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void create_slotNotOpen_throwsSlotFull() {
        slot.setStatus(LessonSlotStatus.FULL);

        assertThatThrownBy(() -> bookingService.create(50L, request(1)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("no longer open");
    }

    @Test
    void create_exceedsRemainingCapacity_throwsSlotFull() {
        slot.setBookedCount(4);
        slot.setCapacity(5);

        assertThatThrownBy(() -> bookingService.create(50L, request(2)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_schoolNotApproved_throwsSchoolNotApproved() {
        school.setStatus(SchoolStatus.SUSPENDED);

        assertThatThrownBy(() -> bookingService.create(50L, request(1)))
                .isInstanceOf(SchoolNotApprovedException.class);
    }

    @Test
    void create_equipmentFromDifferentSchool_throwsConflict() {
        SurfSchool otherSchool = new SurfSchool();
        otherSchool.setId(999L);
        Equipment foreignEquipment = new Equipment();
        foreignEquipment.setId(70L);
        foreignEquipment.setSchool(otherSchool);
        foreignEquipment.setAvailable(true);
        foreignEquipment.setTotalQuantity(10);
        when(equipmentRepository.findById(70L)).thenReturn(Optional.of(foreignEquipment));

        EquipmentLineItemRequest line = new EquipmentLineItemRequest();
        line.setEquipmentId(70L);
        line.setQuantity(1);
        line.setRentalUnit(RentalUnit.DAY);
        CreateBookingRequest req = request(1);
        req.setEquipmentItems(List.of(line));

        assertThatThrownBy(() -> bookingService.create(50L, req)).isInstanceOf(ConflictException.class);
    }

    @Test
    void create_equipmentQuantityExceedsStock_throwsConflict() {
        Equipment board = new Equipment();
        board.setId(80L);
        board.setSchool(school);
        board.setAvailable(true);
        board.setTotalQuantity(2);
        board.setRentalPricePerHour(BigDecimal.ONE);
        board.setRentalPricePerDay(BigDecimal.TEN);
        when(equipmentRepository.findById(80L)).thenReturn(Optional.of(board));

        EquipmentLineItemRequest line = new EquipmentLineItemRequest();
        line.setEquipmentId(80L);
        line.setQuantity(5);
        line.setRentalUnit(RentalUnit.HOUR);
        CreateBookingRequest req = request(1);
        req.setEquipmentItems(List.of(line));

        assertThatThrownBy(() -> bookingService.create(50L, req)).isInstanceOf(ConflictException.class);
    }

    // --- transition matrix ---

    private Booking bookingWithStatus(BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setSchool(school);
        booking.setSurfer(surfer);
        booking.setLessonSlot(slot);
        booking.setInstructor(instructor);
        booking.setParticipants(1);
        booking.setTotalPrice(BigDecimal.valueOf(40));
        booking.setCurrency(Currency.USD);
        booking.setStatus(status);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        return booking;
    }

    @Test
    void accept_fromPending_succeedsAndIncrementsCapacity() {
        bookingWithStatus(BookingStatus.PENDING);

        BookingDto dto = bookingService.accept(1L, 100L);

        assertThat(dto.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        verify(lessonSlotCapacityService).increment(40L, 1);
        verify(notificationService).notify(org.mockito.ArgumentMatchers.eq(surfer),
                org.mockito.ArgumentMatchers.eq(com.example.aquaventure.entity.NotificationType.BOOKING_ACCEPTED), any());
    }

    @Test
    void accept_wrongSchool_throwsOwnershipViolation() {
        bookingWithStatus(BookingStatus.PENDING);
        SurfSchool otherSchool = new SurfSchool();
        otherSchool.setId(999L);
        when(surfSchoolService.getOwnedSchoolOrThrow(2L)).thenReturn(otherSchool);

        assertThatThrownBy(() -> bookingService.accept(2L, 100L)).isInstanceOf(OwnershipViolationException.class);
    }

    @Test
    void accept_retriesOnOptimisticLockThenSucceeds() {
        bookingWithStatus(BookingStatus.PENDING);
        doThrow(new ObjectOptimisticLockingFailureException(LessonSlot.class, 40L))
                .doThrow(new ObjectOptimisticLockingFailureException(LessonSlot.class, 40L))
                .doNothing()
                .when(lessonSlotCapacityService).increment(40L, 1);

        BookingDto dto = bookingService.accept(1L, 100L);

        assertThat(dto.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
    }

    @Test
    void accept_exhaustsRetries_throwsConcurrentModification() {
        bookingWithStatus(BookingStatus.PENDING);
        doThrow(new ObjectOptimisticLockingFailureException(LessonSlot.class, 40L))
                .when(lessonSlotCapacityService).increment(40L, 1);

        assertThatThrownBy(() -> bookingService.accept(1L, 100L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("concurrently");
    }

    @Test
    void accept_slotFullDoesNotRetry() {
        bookingWithStatus(BookingStatus.PENDING);
        doThrow(new ConflictException("SLOT_FULL", "no capacity")).when(lessonSlotCapacityService).increment(40L, 1);

        assertThatThrownBy(() -> bookingService.accept(1L, 100L)).isInstanceOf(ConflictException.class);
        verify(lessonSlotCapacityService, org.mockito.Mockito.times(1)).increment(40L, 1);
    }

    @Test
    void accept_alreadyAccepted_throwsInvalidTransition() {
        bookingWithStatus(BookingStatus.ACCEPTED);

        assertThatThrownBy(() -> bookingService.accept(1L, 100L)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void reject_fromPending_succeeds() {
        bookingWithStatus(BookingStatus.PENDING);

        BookingDto dto = bookingService.reject(1L, 100L, "No instructor available");

        assertThat(dto.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(dto.getSchoolResponseNote()).isEqualTo("No instructor available");
    }

    @Test
    void reject_fromAccepted_throwsInvalidTransition() {
        bookingWithStatus(BookingStatus.ACCEPTED);

        assertThatThrownBy(() -> bookingService.reject(1L, 100L, "too late")).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void complete_fromAccepted_succeedsAndPublishesEvent() {
        bookingWithStatus(BookingStatus.ACCEPTED);

        BookingDto dto = bookingService.complete(1L, 100L);

        assertThat(dto.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        verify(eventPublisher).publishEvent(any(BookingCompletedEvent.class));
    }

    @Test
    void complete_fromPending_throwsInvalidTransition() {
        bookingWithStatus(BookingStatus.PENDING);

        assertThatThrownBy(() -> bookingService.complete(1L, 100L)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancel_fromPending_succeedsWithoutReleasingCapacity() {
        bookingWithStatus(BookingStatus.PENDING);

        BookingDto dto = bookingService.cancel(50L, 100L);

        assertThat(dto.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(lessonSlotCapacityService, never()).release(any(), any(Integer.class));
    }

    @Test
    void cancel_fromAccepted_releasesCapacity() {
        bookingWithStatus(BookingStatus.ACCEPTED);

        bookingService.cancel(50L, 100L);

        verify(lessonSlotCapacityService).release(40L, 1);
    }

    @Test
    void cancel_wrongSurfer_throwsOwnershipViolation() {
        bookingWithStatus(BookingStatus.PENDING);

        assertThatThrownBy(() -> bookingService.cancel(999L, 100L)).isInstanceOf(OwnershipViolationException.class);
    }

    @Test
    void cancel_fromCompleted_throwsInvalidTransition() {
        bookingWithStatus(BookingStatus.COMPLETED);

        assertThatThrownBy(() -> bookingService.cancel(50L, 100L)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancel_afterSlotStarted_throwsConflict() {
        Booking booking = bookingWithStatus(BookingStatus.PENDING);
        booking.getLessonSlot().setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));

        assertThatThrownBy(() -> bookingService.cancel(50L, 100L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("started");
    }

    @Test
    void cancel_fromRejected_throwsInvalidTransition() {
        bookingWithStatus(BookingStatus.REJECTED);

        assertThatThrownBy(() -> bookingService.cancel(50L, 100L)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void reject_fromCancelled_throwsInvalidTransition() {
        bookingWithStatus(BookingStatus.CANCELLED);

        assertThatThrownBy(() -> bookingService.reject(1L, 100L, "note")).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void accept_fromRejected_throwsInvalidTransition() {
        bookingWithStatus(BookingStatus.REJECTED);

        assertThatThrownBy(() -> bookingService.accept(1L, 100L)).isInstanceOf(InvalidStateTransitionException.class);
    }
}
