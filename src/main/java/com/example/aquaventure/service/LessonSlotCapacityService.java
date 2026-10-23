package com.example.aquaventure.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonSlotStatus;
import com.example.aquaventure.exception.ConflictException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.repository.LessonSlotRepository;

/**
 * Each method runs in its own fresh transaction (REQUIRES_NEW) so that a caller can retry on
 * {@link org.springframework.orm.ObjectOptimisticLockingFailureException} and actually observe
 * the latest committed version on each attempt (self-invocation within one transaction would
 * keep reading the same cached entity).
 */
@Service
public class LessonSlotCapacityService {

    private final LessonSlotRepository lessonSlotRepository;

    public LessonSlotCapacityService(LessonSlotRepository lessonSlotRepository) {
        this.lessonSlotRepository = lessonSlotRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increment(Long slotId, int participants) {
        LessonSlot slot = lessonSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson slot not found: " + slotId));
        if (slot.getStatus() == LessonSlotStatus.CANCELLED) {
            throw new ConflictException("SLOT_CANCELLED", "This slot has been cancelled");
        }
        if (slot.getBookedCount() + participants > slot.getCapacity()) {
            throw new ConflictException("SLOT_FULL", "This slot no longer has enough capacity");
        }
        slot.setBookedCount(slot.getBookedCount() + participants);
        if (slot.getBookedCount() >= slot.getCapacity()) {
            slot.setStatus(LessonSlotStatus.FULL);
        }
        lessonSlotRepository.save(slot);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(Long slotId, int participants) {
        LessonSlot slot = lessonSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson slot not found: " + slotId));
        slot.setBookedCount(Math.max(0, slot.getBookedCount() - participants));
        if (slot.getStatus() == LessonSlotStatus.FULL && slot.getBookedCount() < slot.getCapacity()) {
            slot.setStatus(LessonSlotStatus.OPEN);
        }
        lessonSlotRepository.save(slot);
    }
}
