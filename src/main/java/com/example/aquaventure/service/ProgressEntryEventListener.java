package com.example.aquaventure.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.aquaventure.entity.Booking;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.ProgressEntry;
import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.ProgressEntryRepository;

/**
 * Listens for {@link BookingCompletedEvent} (published by BookingService.complete()) and creates
 * a draft progress entry for the surfer to fill in. Kept entirely separate from BookingService so
 * that service never needs to know this feature exists.
 */
@Component
public class ProgressEntryEventListener {

    private final BookingRepository bookingRepository;
    private final ProgressEntryRepository progressEntryRepository;

    public ProgressEntryEventListener(BookingRepository bookingRepository, ProgressEntryRepository progressEntryRepository) {
        this.bookingRepository = bookingRepository;
        this.progressEntryRepository = progressEntryRepository;
    }

    // The outer transaction (BookingService.complete()) has already committed by the time this
    // runs, and its connection/session is still being unbound from the thread at that exact
    // moment - joining it with a plain @Transactional (REQUIRED) silently no-ops the write
    // (save() returns with a null id, nothing is flushed). REQUIRES_NEW forces a genuinely
    // separate transaction/connection so the insert actually happens.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBookingCompleted(BookingCompletedEvent event) {
        if (progressEntryRepository.findByBookingId(event.bookingId()).isPresent()) {
            return;
        }

        Booking booking = bookingRepository.findById(event.bookingId()).orElse(null);
        if (booking == null) {
            return;
        }

        LessonSlot slot = booking.getLessonSlot();
        ProgressEntry entry = new ProgressEntry();
        entry.setSurfer(booking.getSurfer());
        entry.setBooking(booking);
        entry.setDate(slot.getStartTime());
        entry.setLocation(slot.getLocation() != null ? slot.getLocation() : slot.getLesson().getLocation());
        entry.setSkillFocus(slot.getLesson().getTitle());
        entry.setDurationMinutes(slot.getLesson().getDurationMinutes());
        entry.setDraft(true);
        progressEntryRepository.save(entry);
    }
}
