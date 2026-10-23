package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.aquaventure.dto.surfer.UpdateSurferProfileRequest;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurferProfile;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.SurferProfileRepository;
import com.example.aquaventure.repository.UserRepository;

class SurferProfileServiceTest {

    private final SurferProfileRepository surferProfileRepository = mock(SurferProfileRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SurfLocationService surfLocationService = mock(SurfLocationService.class);
    private final SurferProfileService service = new SurferProfileService(surferProfileRepository, userRepository, surfLocationService);

    @Test
    void getOrCreateDefault_whenAbsent_createsDefaultBeginnerProfile() {
        when(surferProfileRepository.findByUserId(7L)).thenReturn(Optional.empty());
        User user = new User();
        user.setId(7L);
        user.setRole(Role.SURFER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(surferProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.getOrCreateDefault(7L);

        assertThat(dto.getSkillLevel()).isEqualTo(SkillLevel.BEGINNER);
        verify(surferProfileRepository).save(any());
    }

    @Test
    void getOrCreateDefault_whenPresent_returnsExistingWithoutCreating() {
        SurferProfile existing = new SurferProfile();
        existing.setSkillLevel(SkillLevel.ADVANCED);
        when(surferProfileRepository.findByUserId(8L)).thenReturn(Optional.of(existing));

        var dto = service.getOrCreateDefault(8L);

        assertThat(dto.getSkillLevel()).isEqualTo(SkillLevel.ADVANCED);
    }

    @Test
    void update_savesNewPreferences() {
        SurferProfile existing = new SurferProfile();
        existing.setSkillLevel(SkillLevel.BEGINNER);
        when(surferProfileRepository.findByUserId(9L)).thenReturn(Optional.of(existing));
        when(surferProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateSurferProfileRequest request = new UpdateSurferProfileRequest();
        request.setSkillLevel(SkillLevel.INTERMEDIATE);
        request.setBio("Loving the waves");

        var dto = service.update(9L, request);

        assertThat(dto.getSkillLevel()).isEqualTo(SkillLevel.INTERMEDIATE);
        assertThat(dto.getBio()).isEqualTo("Loving the waves");
    }
}
