package com.example.aquaventure.dto.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.entity.Currency;

public class BookingDto {

    private Long id;
    private Long schoolId;
    private String schoolName;
    private Long lessonSlotId;
    private String lessonTitle;
    private Instant slotStartTime;
    private Instant slotEndTime;
    private Long instructorId;
    private String instructorName;
    private Integer participants;
    private List<BookingEquipmentItemDto> equipmentItems;
    private BigDecimal totalPrice;
    private Currency currency;
    private String surferNote;
    private BookingStatus status;
    private String schoolResponseNote;
    private Instant respondedAt;
    private Instant completedAt;
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public Long getLessonSlotId() {
        return lessonSlotId;
    }

    public void setLessonSlotId(Long lessonSlotId) {
        this.lessonSlotId = lessonSlotId;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public Instant getSlotStartTime() {
        return slotStartTime;
    }

    public void setSlotStartTime(Instant slotStartTime) {
        this.slotStartTime = slotStartTime;
    }

    public Instant getSlotEndTime() {
        return slotEndTime;
    }

    public void setSlotEndTime(Instant slotEndTime) {
        this.slotEndTime = slotEndTime;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public List<BookingEquipmentItemDto> getEquipmentItems() {
        return equipmentItems;
    }

    public void setEquipmentItems(List<BookingEquipmentItemDto> equipmentItems) {
        this.equipmentItems = equipmentItems;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getSurferNote() {
        return surferNote;
    }

    public void setSurferNote(String surferNote) {
        this.surferNote = surferNote;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getSchoolResponseNote() {
        return schoolResponseNote;
    }

    public void setSchoolResponseNote(String schoolResponseNote) {
        this.schoolResponseNote = schoolResponseNote;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Instant respondedAt) {
        this.respondedAt = respondedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
