package com.example.aquaventure.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.surfer.RecommendationDto;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.SurferProfile;
import com.example.aquaventure.repository.LessonRepository;
import com.example.aquaventure.repository.SurferProfileRepository;
import com.example.aquaventure.repository.spec.LessonSpecifications;

@Service
public class RecommendationService {

    private final SurferProfileRepository surferProfileRepository;
    private final LessonRepository lessonRepository;

    public RecommendationService(SurferProfileRepository surferProfileRepository, LessonRepository lessonRepository) {
        this.surferProfileRepository = surferProfileRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationDto> recommendFor(Long userId) {
        SurferProfile profile = surferProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return List.of();
        }

        Long preferredLocationId = profile.getPreferredLocation() != null ? profile.getPreferredLocation().getId() : null;

        Specification<Lesson> spec = Specification.<Lesson>where(LessonSpecifications.isActiveAtApprovedSchool())
                .and(LessonSpecifications.hasSkillLevel(profile.getSkillLevel()));

        List<Lesson> matches = lessonRepository.findAll(
                preferredLocationId != null ? spec.and(LessonSpecifications.atLocation(preferredLocationId)) : spec);

        if (matches.isEmpty() && preferredLocationId != null) {
            // Relax the location filter rather than return nothing.
            matches = lessonRepository.findAll(spec);
        }

        return matches.stream().map(lesson -> toRecommendation(lesson, profile)).toList();
    }

    private RecommendationDto toRecommendation(Lesson lesson, SurferProfile profile) {
        RecommendationDto dto = new RecommendationDto();
        dto.setLessonId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setSchoolId(lesson.getSchool().getId());
        dto.setSchoolName(lesson.getSchool().getName());
        dto.setLocationName(lesson.getLocation() != null ? lesson.getLocation().getName() : null);
        dto.setSkillLevel(lesson.getSkillLevel());
        dto.setPrice(lesson.getPrice());
        dto.setCurrency(lesson.getCurrency());

        boolean locationMatch = profile.getPreferredLocation() != null && lesson.getLocation() != null
                && profile.getPreferredLocation().getId().equals(lesson.getLocation().getId());
        dto.setMatchReason(locationMatch
                ? "Matches your skill level and preferred location"
                : "Matches your skill level");
        return dto;
    }
}
