package com.example.aquaventure.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.school.LessonDto;
import com.example.aquaventure.dto.school.UpsertLessonRequest;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.LessonMapper;
import com.example.aquaventure.repository.LessonRepository;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SurfSchoolService surfSchoolService;
    private final SurfLocationService surfLocationService;

    public LessonService(LessonRepository lessonRepository, SurfSchoolService surfSchoolService,
                          SurfLocationService surfLocationService) {
        this.lessonRepository = lessonRepository;
        this.surfSchoolService = surfSchoolService;
        this.surfLocationService = surfLocationService;
    }

    @Transactional(readOnly = true)
    public Page<LessonDto> list(Long ownerId, Pageable pageable) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        return lessonRepository.findBySchoolId(school.getId(), pageable).map(LessonMapper::toDto);
    }

    @Transactional
    public LessonDto create(Long ownerId, UpsertLessonRequest request) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Lesson lesson = new Lesson();
        lesson.setSchool(school);
        apply(lesson, request);
        return LessonMapper.toDto(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonDto update(Long ownerId, Long lessonId, UpsertLessonRequest request) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Lesson lesson = getOwnedOrThrow(lessonId, school.getId());
        apply(lesson, request);
        return LessonMapper.toDto(lessonRepository.save(lesson));
    }

    @Transactional
    public void delete(Long ownerId, Long lessonId) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Lesson lesson = getOwnedOrThrow(lessonId, school.getId());
        lessonRepository.delete(lesson);
    }

    Lesson getOwnedOrThrow(Long lessonId, Long schoolId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
        if (!lesson.getSchool().getId().equals(schoolId)) {
            throw new OwnershipViolationException("This lesson does not belong to your school");
        }
        return lesson;
    }

    private void apply(Lesson lesson, UpsertLessonRequest request) {
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setLessonType(request.getLessonType());
        lesson.setSkillLevel(request.getSkillLevel());
        lesson.setDurationMinutes(request.getDurationMinutes());
        lesson.setPrice(request.getPrice());
        lesson.setCurrency(request.getCurrency());
        lesson.setMaxParticipants(request.getMaxParticipants());
        lesson.setIncludesEquipment(request.isIncludesEquipment());
        lesson.setLocation(request.getLocationId() != null
                ? surfLocationService.getByIdOrThrow(request.getLocationId()) : null);
        lesson.setActive(request.isActive());
    }
}
