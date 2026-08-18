package com.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.SurfProgressRequest;
import com.aquaventure.dto.SurfProgressResponse;
import com.aquaventure.entity.Instructor;
import com.aquaventure.entity.SurfProgress;
import com.aquaventure.entity.Tourist;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.repository.InstructorRepository;
import com.aquaventure.repository.SurfProgressRepository;

@Service
@Transactional
public class SurfProgressService {

    @Autowired
    private SurfProgressRepository surfProgressRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private TouristService touristService;

    public List<SurfProgressResponse> listForTourist(Long touristId) {
        Tourist tourist = touristService.getEntity(touristId);
        return surfProgressRepository.findByTouristOrderBySessionDateDesc(tourist).stream()
                .map(this::toResponse)
                .toList();
    }

    public SurfProgressResponse create(User instructorUser, SurfProgressRequest request) {
        Instructor instructor = getInstructorForUser(instructorUser);
        Tourist tourist = touristService.getEntity(request.getTouristId());

        SurfProgress progress = new SurfProgress();
        progress.setTourist(tourist);
        progress.setInstructor(instructor);
        progress.setSessionDate(request.getSessionDate());
        progress.setSkillLevel(request.getSkillLevel());
        progress.setNotes(request.getNotes());
        return toResponse(surfProgressRepository.save(progress));
    }

    public SurfProgressResponse update(User instructorUser, Long id, SurfProgressRequest request) {
        Instructor instructor = getInstructorForUser(instructorUser);
        SurfProgress progress = surfProgressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surf progress entry not found: " + id));

        if (progress.getInstructor() == null
                || !progress.getInstructor().getInstructorId().equals(instructor.getInstructorId())) {
            throw new ForbiddenActionException("This progress entry does not belong to you");
        }

        progress.setSessionDate(request.getSessionDate());
        progress.setSkillLevel(request.getSkillLevel());
        progress.setNotes(request.getNotes());
        return toResponse(surfProgressRepository.save(progress));
    }

    private Instructor getInstructorForUser(User user) {
        return instructorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor profile not found"));
    }

    private SurfProgressResponse toResponse(SurfProgress progress) {
        return new SurfProgressResponse(
                progress.getProgressId(),
                progress.getTourist().getTouristId(),
                progress.getInstructor() != null ? progress.getInstructor().getInstructorId() : null,
                progress.getInstructor() != null ? progress.getInstructor().getName() : null,
                progress.getSessionDate(),
                progress.getSkillLevel(),
                progress.getNotes());
    }
}
