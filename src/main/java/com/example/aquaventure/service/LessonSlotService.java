package com.example.aquaventure.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.school.LessonSlotDto;
import com.example.aquaventure.dto.school.UpsertLessonSlotRequest;
import com.example.aquaventure.entity.Instructor;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.exception.ConflictException;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.LessonSlotMapper;
import com.example.aquaventure.repository.InstructorRepository;
import com.example.aquaventure.repository.LessonSlotRepository;

@Service
public class LessonSlotService {

    private final LessonSlotRepository lessonSlotRepository;
    private final InstructorRepository instructorRepository;
    private final SurfSchoolService surfSchoolService;
    private final LessonService lessonService;
    private final SurfLocationService surfLocationService;

    public LessonSlotService(LessonSlotRepository lessonSlotRepository, InstructorRepository instructorRepository,
                              SurfSchoolService surfSchoolService, LessonService lessonService,
                              SurfLocationService surfLocationService) {
        this.lessonSlotRepository = lessonSlotRepository;
        this.instructorRepository = instructorRepository;
        this.surfSchoolService = surfSchoolService;
        this.lessonService = lessonService;
        this.surfLocationService = surfLocationService;
    }

    @Transactional(readOnly = true)
    public Page<LessonSlotDto> list(Long ownerId, Pageable pageable) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        return lessonSlotRepository.findByLessonSchoolId(school.getId(), pageable).map(LessonSlotMapper::toDto);
    }

    @Transactional
    public LessonSlotDto create(Long ownerId, UpsertLessonSlotRequest request) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        validateTimeRange(request);

        Lesson lesson = lessonService.getOwnedOrThrow(request.getLessonId(), school.getId());
        Instructor instructor = getOwnedInstructorOrThrow(request.getInstructorId(), school.getId());
        checkNoOverlap(instructor.getId(), request.getStartTime(), request.getEndTime(), null);

        LessonSlot slot = new LessonSlot();
        slot.setLesson(lesson);
        slot.setInstructor(instructor);
        slot.setLocation(request.getLocationId() != null
                ? surfLocationService.getByIdOrThrow(request.getLocationId()) : lesson.getLocation());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacity(request.getCapacity());
        return LessonSlotMapper.toDto(lessonSlotRepository.save(slot));
    }

    @Transactional
    public LessonSlotDto update(Long ownerId, Long slotId, UpsertLessonSlotRequest request) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        validateTimeRange(request);

        LessonSlot slot = getOwnedOrThrow(slotId, school.getId());
        Lesson lesson = lessonService.getOwnedOrThrow(request.getLessonId(), school.getId());
        Instructor instructor = getOwnedInstructorOrThrow(request.getInstructorId(), school.getId());
        checkNoOverlap(instructor.getId(), request.getStartTime(), request.getEndTime(), slot.getId());

        slot.setLesson(lesson);
        slot.setInstructor(instructor);
        slot.setLocation(request.getLocationId() != null
                ? surfLocationService.getByIdOrThrow(request.getLocationId()) : lesson.getLocation());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacity(request.getCapacity());
        return LessonSlotMapper.toDto(lessonSlotRepository.save(slot));
    }

    @Transactional
    public void delete(Long ownerId, Long slotId) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        LessonSlot slot = getOwnedOrThrow(slotId, school.getId());
        if (slot.getBookedCount() > 0) {
            throw new ConflictException("SLOT_HAS_BOOKINGS", "Cannot delete a slot that already has bookings");
        }
        lessonSlotRepository.delete(slot);
    }

    private LessonSlot getOwnedOrThrow(Long slotId, Long schoolId) {
        LessonSlot slot = lessonSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson slot not found: " + slotId));
        if (!slot.getLesson().getSchool().getId().equals(schoolId)) {
            throw new OwnershipViolationException("This lesson slot does not belong to your school");
        }
        return slot;
    }

    private Instructor getOwnedInstructorOrThrow(Long instructorId, Long schoolId) {
        return instructorRepository.findByIdAndSchoolId(instructorId, schoolId)
                .orElseThrow(() -> new OwnershipViolationException("This instructor does not belong to your school"));
    }

    private void validateTimeRange(UpsertLessonSlotRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ConflictException("INVALID_TIME_RANGE", "endTime must be after startTime");
        }
    }

    private void checkNoOverlap(Long instructorId, java.time.Instant start, java.time.Instant end, Long excludeId) {
        if (lessonSlotRepository.existsOverlap(instructorId, start, end, excludeId)) {
            throw new ConflictException("SLOT_OVERLAP", "This instructor already has an overlapping slot");
        }
    }
}
