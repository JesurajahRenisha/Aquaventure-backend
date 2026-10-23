package com.example.aquaventure.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.surfer.SurferProfileDto;
import com.example.aquaventure.dto.surfer.UpdateSurferProfileRequest;
import com.example.aquaventure.entity.SurferProfile;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.SurferProfileMapper;
import com.example.aquaventure.repository.SurferProfileRepository;
import com.example.aquaventure.repository.UserRepository;

@Service
public class SurferProfileService {

    private final SurferProfileRepository surferProfileRepository;
    private final UserRepository userRepository;
    private final SurfLocationService surfLocationService;

    public SurferProfileService(SurferProfileRepository surferProfileRepository, UserRepository userRepository,
                                 SurfLocationService surfLocationService) {
        this.surferProfileRepository = surferProfileRepository;
        this.userRepository = userRepository;
        this.surfLocationService = surfLocationService;
    }

    @Transactional
    public SurferProfileDto getOrCreateDefault(Long userId) {
        return SurferProfileMapper.toDto(getOrCreateEntity(userId));
    }

    @Transactional
    public SurferProfileDto update(Long userId, UpdateSurferProfileRequest request) {
        SurferProfile profile = getOrCreateEntity(userId);
        profile.setSkillLevel(request.getSkillLevel());
        profile.setPreferredLocation(request.getPreferredLocationId() != null
                ? surfLocationService.getByIdOrThrow(request.getPreferredLocationId()) : null);
        profile.setPreferredLessonType(request.getPreferredLessonType());
        profile.setBudgetMin(request.getBudgetMin());
        profile.setBudgetMax(request.getBudgetMax());
        profile.setBoardTypePreference(request.getBoardTypePreference());
        profile.setBio(request.getBio());
        return SurferProfileMapper.toDto(surferProfileRepository.save(profile));
    }

    private SurferProfile getOrCreateEntity(Long userId) {
        return surferProfileRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            SurferProfile profile = new SurferProfile();
            profile.setUser(user);
            return surferProfileRepository.save(profile);
        });
    }
}
