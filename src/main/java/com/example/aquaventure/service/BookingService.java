package com.example.aquaventure.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.booking.BookingDto;
import com.example.aquaventure.dto.booking.CreateBookingRequest;
import com.example.aquaventure.dto.booking.EquipmentLineItemRequest;
import com.example.aquaventure.entity.Booking;
import com.example.aquaventure.entity.BookingEquipmentItem;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.entity.Equipment;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonSlotStatus;
import com.example.aquaventure.entity.NotificationType;
import com.example.aquaventure.entity.RentalUnit;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.ConflictException;
import com.example.aquaventure.exception.InvalidStateTransitionException;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.exception.SchoolNotApprovedException;
import com.example.aquaventure.mapper.BookingMapper;
import com.example.aquaventure.repository.BookingEquipmentItemRepository;
import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.EquipmentRepository;
import com.example.aquaventure.repository.LessonSlotRepository;
import com.example.aquaventure.repository.UserRepository;

@Service
public class BookingService {

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(BookingStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(BookingStatus.PENDING,
                Set.of(BookingStatus.ACCEPTED, BookingStatus.REJECTED, BookingStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(BookingStatus.ACCEPTED, Set.of(BookingStatus.CANCELLED, BookingStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(BookingStatus.REJECTED, Set.of());
        ALLOWED_TRANSITIONS.put(BookingStatus.CANCELLED, Set.of());
        ALLOWED_TRANSITIONS.put(BookingStatus.COMPLETED, Set.of());
    }

    private static final int MAX_LOCK_RETRIES = 3;

    private final BookingRepository bookingRepository;
    private final BookingEquipmentItemRepository bookingEquipmentItemRepository;
    private final LessonSlotRepository lessonSlotRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final SurfSchoolService surfSchoolService;
    private final LessonSlotCapacityService lessonSlotCapacityService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository, BookingEquipmentItemRepository bookingEquipmentItemRepository,
                           LessonSlotRepository lessonSlotRepository, EquipmentRepository equipmentRepository,
                           UserRepository userRepository, SurfSchoolService surfSchoolService,
                           LessonSlotCapacityService lessonSlotCapacityService, NotificationService notificationService,
                           ApplicationEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.bookingEquipmentItemRepository = bookingEquipmentItemRepository;
        this.lessonSlotRepository = lessonSlotRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
        this.surfSchoolService = surfSchoolService;
        this.lessonSlotCapacityService = lessonSlotCapacityService;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookingDto create(Long surferId, CreateBookingRequest request) {
        LessonSlot slot = lessonSlotRepository.findById(request.getLessonSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson slot not found: " + request.getLessonSlotId()));

        SurfSchool school = slot.getLesson().getSchool();
        if (school.getStatus() != SchoolStatus.APPROVED) {
            throw new SchoolNotApprovedException("This school is not currently accepting bookings");
        }
        if (slot.getStatus() != LessonSlotStatus.OPEN) {
            throw new ConflictException("SLOT_FULL", "This slot is no longer open for booking");
        }
        if (slot.getBookedCount() + request.getParticipants() > slot.getCapacity()) {
            throw new ConflictException("SLOT_FULL", "This slot does not have enough remaining capacity");
        }

        User surfer = userRepository.findById(surferId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + surferId));

        BigDecimal total = slot.getLesson().getPrice().multiply(BigDecimal.valueOf(request.getParticipants()));
        List<BookingEquipmentItem> items = new ArrayList<>();
        if (request.getEquipmentItems() != null) {
            for (EquipmentLineItemRequest line : request.getEquipmentItems()) {
                Equipment equipment = equipmentRepository.findById(line.getEquipmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + line.getEquipmentId()));
                if (!equipment.getSchool().getId().equals(school.getId())) {
                    throw new ConflictException("EQUIPMENT_NOT_AVAILABLE", "Equipment does not belong to this school");
                }
                if (!equipment.isAvailable() || line.getQuantity() > equipment.getTotalQuantity()) {
                    throw new ConflictException("EQUIPMENT_NOT_AVAILABLE",
                            "Equipment \"" + equipment.getName() + "\" is not available in the requested quantity");
                }
                BigDecimal unitPrice = line.getRentalUnit() == RentalUnit.HOUR
                        ? equipment.getRentalPricePerHour() : equipment.getRentalPricePerDay();
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.getQuantity()));

                BookingEquipmentItem item = new BookingEquipmentItem();
                item.setEquipment(equipment);
                item.setQuantity(line.getQuantity());
                item.setRentalUnit(line.getRentalUnit());
                item.setUnitPrice(unitPrice);
                item.setLineTotal(lineTotal);
                items.add(item);
                total = total.add(lineTotal);
            }
        }

        Booking booking = new Booking();
        booking.setSurfer(surfer);
        booking.setSchool(school);
        booking.setLessonSlot(slot);
        booking.setInstructor(slot.getInstructor());
        booking.setParticipants(request.getParticipants());
        booking.setTotalPrice(total);
        booking.setCurrency(slot.getLesson().getCurrency());
        booking.setSurferNote(request.getNote());
        booking.setStatus(BookingStatus.PENDING);
        booking = bookingRepository.save(booking);

        for (BookingEquipmentItem item : items) {
            item.setBooking(booking);
        }
        bookingEquipmentItemRepository.saveAll(items);

        notificationService.notify(school.getOwner(), NotificationType.BOOKING_CREATED,
                "New booking request for \"" + slot.getLesson().getTitle() + "\" from " + surfer.getFirstname());

        return BookingMapper.toDto(booking, items);
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> listForSurfer(Long surferId, BookingStatus status, Pageable pageable) {
        Page<Booking> page = status != null
                ? bookingRepository.findBySurferIdAndStatus(surferId, status, pageable)
                : bookingRepository.findBySurferId(surferId, pageable);
        return page.map(this::toDtoWithItems);
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> listForSchool(Long ownerId, BookingStatus status, Pageable pageable) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        Page<Booking> page = status != null
                ? bookingRepository.findBySchoolIdAndStatus(school.getId(), status, pageable)
                : bookingRepository.findBySchoolId(school.getId(), pageable);
        return page.map(this::toDtoWithItems);
    }

    @Transactional
    public BookingDto accept(Long ownerId, Long bookingId) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        Booking booking = getOwnedBySchoolOrThrow(bookingId, school.getId());
        validateTransition(booking.getStatus(), BookingStatus.ACCEPTED);

        incrementCapacityWithRetry(booking.getLessonSlot().getId(), booking.getParticipants());

        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setRespondedAt(Instant.now());
        booking = bookingRepository.save(booking);

        notificationService.notify(booking.getSurfer(), NotificationType.BOOKING_ACCEPTED,
                "Your booking for \"" + booking.getLessonSlot().getLesson().getTitle() + "\" has been accepted");

        return toDtoWithItems(booking);
    }

    @Transactional
    public BookingDto reject(Long ownerId, Long bookingId, String note) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        Booking booking = getOwnedBySchoolOrThrow(bookingId, school.getId());
        validateTransition(booking.getStatus(), BookingStatus.REJECTED);

        booking.setStatus(BookingStatus.REJECTED);
        booking.setSchoolResponseNote(note);
        booking.setRespondedAt(Instant.now());
        booking = bookingRepository.save(booking);

        notificationService.notify(booking.getSurfer(), NotificationType.BOOKING_REJECTED,
                "Your booking for \"" + booking.getLessonSlot().getLesson().getTitle() + "\" was rejected: " + note);

        return toDtoWithItems(booking);
    }

    @Transactional
    public BookingDto complete(Long ownerId, Long bookingId) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        Booking booking = getOwnedBySchoolOrThrow(bookingId, school.getId());
        validateTransition(booking.getStatus(), BookingStatus.COMPLETED);

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(Instant.now());
        booking = bookingRepository.save(booking);

        notificationService.notify(booking.getSurfer(), NotificationType.BOOKING_COMPLETED,
                "Your booking for \"" + booking.getLessonSlot().getLesson().getTitle() + "\" is complete. Add your progress notes!");
        eventPublisher.publishEvent(new BookingCompletedEvent(booking.getId()));

        return toDtoWithItems(booking);
    }

    @Transactional
    public BookingDto cancel(Long surferId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        if (!booking.getSurfer().getId().equals(surferId)) {
            throw new OwnershipViolationException("This booking does not belong to you");
        }
        validateTransition(booking.getStatus(), BookingStatus.CANCELLED);
        if (!booking.getLessonSlot().getStartTime().isAfter(Instant.now())) {
            throw new ConflictException("BOOKING_ALREADY_STARTED", "Cannot cancel a booking after its slot has started");
        }

        if (booking.getStatus() == BookingStatus.ACCEPTED) {
            releaseCapacityWithRetry(booking.getLessonSlot().getId(), booking.getParticipants());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        notificationService.notify(booking.getSchool().getOwner(), NotificationType.BOOKING_CANCELLED,
                "A booking for \"" + booking.getLessonSlot().getLesson().getTitle() + "\" was cancelled by the surfer");

        return toDtoWithItems(booking);
    }

    private void incrementCapacityWithRetry(Long slotId, int participants) {
        int attempts = 0;
        while (true) {
            try {
                lessonSlotCapacityService.increment(slotId, participants);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_LOCK_RETRIES) {
                    throw new ConflictException("CONCURRENT_MODIFICATION",
                            "This slot was modified concurrently, please retry");
                }
            }
        }
    }

    private void releaseCapacityWithRetry(Long slotId, int participants) {
        int attempts = 0;
        while (true) {
            try {
                lessonSlotCapacityService.release(slotId, participants);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_LOCK_RETRIES) {
                    throw new ConflictException("CONCURRENT_MODIFICATION",
                            "This slot was modified concurrently, please retry");
                }
            }
        }
    }

    private Booking getOwnedBySchoolOrThrow(Long bookingId, Long schoolId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        if (!booking.getSchool().getId().equals(schoolId)) {
            throw new OwnershipViolationException("This booking does not belong to your school");
        }
        return booking;
    }

    private void validateTransition(BookingStatus current, BookingStatus target) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new InvalidStateTransitionException("Cannot transition booking from " + current + " to " + target);
        }
    }

    private BookingDto toDtoWithItems(Booking booking) {
        return BookingMapper.toDto(booking, bookingEquipmentItemRepository.findByBookingId(booking.getId()));
    }
}
