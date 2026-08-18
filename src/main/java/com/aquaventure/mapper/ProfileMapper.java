package com.aquaventure.mapper;

import com.aquaventure.dto.InstructorResponse;
import com.aquaventure.dto.ProviderResponse;
import com.aquaventure.dto.TouristResponse;
import com.aquaventure.entity.Instructor;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.Tourist;

public final class ProfileMapper {

    private ProfileMapper() {
    }

    public static TouristResponse toResponse(Tourist tourist) {
        return new TouristResponse(
                tourist.getTouristId(),
                tourist.getUser().getUserId(),
                tourist.getUser().getName(),
                tourist.getUser().getEmail(),
                tourist.getSkillLevel(),
                tourist.getExperience());
    }

    public static ProviderResponse toResponse(Provider provider) {
        return new ProviderResponse(
                provider.getProviderId(),
                provider.getUser().getUserId(),
                provider.getUser().getName(),
                provider.getUser().getEmail(),
                provider.getBusinessName(),
                provider.getContactDetails(),
                provider.getLocation());
    }

    public static InstructorResponse toResponse(Instructor instructor) {
        return new InstructorResponse(
                instructor.getInstructorId(),
                instructor.getProvider().getProviderId(),
                instructor.getName(),
                instructor.getCertification(),
                instructor.getExperience(),
                instructor.isAvailability());
    }
}
