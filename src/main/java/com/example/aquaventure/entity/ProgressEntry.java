package com.example.aquaventure.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "progress_entries")
public class ProgressEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surfer_user_id", nullable = false)
    private User surfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @NotNull
    @Column(nullable = false)
    private Instant date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private SurfLocation location;

    @Size(max = 150)
    private String skillFocus;

    private Integer durationMinutes;

    @Min(1)
    @Max(5)
    private Integer selfRating;

    @Column(length = 2000)
    private String notes;

    @Column(length = 2000)
    private String instructorFeedback;

    @Column(name = "is_draft", nullable = false)
    private boolean draft = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSurfer() {
        return surfer;
    }

    public void setSurfer(User surfer) {
        this.surfer = surfer;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public SurfLocation getLocation() {
        return location;
    }

    public void setLocation(SurfLocation location) {
        this.location = location;
    }

    public String getSkillFocus() {
        return skillFocus;
    }

    public void setSkillFocus(String skillFocus) {
        this.skillFocus = skillFocus;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getSelfRating() {
        return selfRating;
    }

    public void setSelfRating(Integer selfRating) {
        this.selfRating = selfRating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInstructorFeedback() {
        return instructorFeedback;
    }

    public void setInstructorFeedback(String instructorFeedback) {
        this.instructorFeedback = instructorFeedback;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }
}
