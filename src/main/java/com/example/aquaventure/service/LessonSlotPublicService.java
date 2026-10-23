package com.example.aquaventure.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.school.LessonSlotDto;
import com.example.aquaventure.entity.LessonSlotStatus;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.LessonSlotMapper;
import com.example.aquaventure.repository.LessonRepository;
import com.example.aquaventure.repository.LessonSlotRepository;

@Service
public class LessonSlotPublicService {

    private final LessonRepository lessonRepository;
    private final LessonSlotRepository lessonSlotRepository;

    public LessonSlotPublicService(LessonRepository lessonRepository, LessonSlotRepository lessonSlotRepository) {
        this.lessonRepository = lessonRepository;
        this.lessonSlotRepository = lessonSlotRepository;
    }

    @Transactional(readOnly = true)
    public List<LessonSlotDto> openSlots(Long lessonId, Instant from, Instant to) {
        lessonRepository.findByIdAndActiveTrueAndSchoolStatus(lessonId, SchoolStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
        return LessonSlotMapper.toDtoList(
                lessonSlotRepository.findByLessonIdAndStatusAndStartTimeBetween(lessonId, LessonSlotStatus.OPEN, from, to));
    }
}
