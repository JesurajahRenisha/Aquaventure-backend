package com.example.aquaventure.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.aquaventure.entity.Booking;
import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.Instructor;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.ProgressEntry;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.ProgressEntryRepository;

class ProgressEntryEventListenerTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final ProgressEntryRepository progressEntryRepository = mock(ProgressEntryRepository.class);
    private final ProgressEntryEventListener listener =
            new ProgressEntryEventListener(bookingRepository, progressEntryRepository);

    private Booking booking() {
        User surfer = new User();
        surfer.setId(1L);

        SurfLocation location = new SurfLocation();
        location.setName("Arugam Bay");

        Lesson lesson = new Lesson();
        lesson.setTitle("Beginner Lesson");
        lesson.setDurationMinutes(90);
        lesson.setLessonType(LessonType.GROUP);
        lesson.setSkillLevel(SkillLevel.BEGINNER);
        lesson.setPrice(BigDecimal.valueOf(40));
        lesson.setCurrency(Currency.USD);
        lesson.setMaxParticipants(5);
        lesson.setLocation(location);

        Instructor instructor = new Instructor();
        instructor.setName("Instructor");

        LessonSlot slot = new LessonSlot();
        slot.setLesson(lesson);
        slot.setInstructor(instructor);
        slot.setLocation(location);
        slot.setStartTime(Instant.parse("2026-11-01T09:00:00Z"));
        slot.setEndTime(Instant.parse("2026-11-01T10:30:00Z"));
        slot.setCapacity(5);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setSurfer(surfer);
        booking.setLessonSlot(slot);
        booking.setInstructor(instructor);
        return booking;
    }

    @Test
    void onBookingCompleted_createsDraftProgressEntry() {
        Booking booking = booking();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(progressEntryRepository.findByBookingId(100L)).thenReturn(Optional.empty());
        when(progressEntryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onBookingCompleted(new BookingCompletedEvent(100L));

        verify(progressEntryRepository).save(org.mockito.ArgumentMatchers.argThat(
                (ProgressEntry e) -> e.isDraft() && e.getSkillFocus().equals("Beginner Lesson")
                        && e.getSurfer().getId().equals(1L)));
    }

    @Test
    void onBookingCompleted_alreadyHasEntry_doesNotCreateDuplicate() {
        when(progressEntryRepository.findByBookingId(100L)).thenReturn(Optional.of(new ProgressEntry()));

        listener.onBookingCompleted(new BookingCompletedEvent(100L));

        verify(progressEntryRepository, never()).save(any());
    }

    @Test
    void onBookingCompleted_bookingMissing_doesNothing() {
        when(progressEntryRepository.findByBookingId(100L)).thenReturn(Optional.empty());
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        listener.onBookingCompleted(new BookingCompletedEvent(100L));

        verify(progressEntryRepository, never()).save(any());
    }
}
