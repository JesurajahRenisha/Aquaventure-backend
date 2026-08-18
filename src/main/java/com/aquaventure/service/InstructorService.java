package com.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.InstructorRequest;
import com.aquaventure.dto.InstructorResponse;
import com.aquaventure.entity.Instructor;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.mapper.ProfileMapper;
import com.aquaventure.repository.InstructorRepository;

@Service
@Transactional
public class InstructorService {

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private ProviderService providerService;

    public List<InstructorResponse> listForProvider(Long providerId) {
        Provider provider = providerService.getEntity(providerId);
        return instructorRepository.findByProvider(provider).stream()
                .map(ProfileMapper::toResponse)
                .toList();
    }

    public Instructor getEntity(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found: " + id));
    }

    public InstructorResponse get(Long id) {
        return ProfileMapper.toResponse(getEntity(id));
    }

    public InstructorResponse create(User providerUser, InstructorRequest request) {
        Provider provider = providerService.getEntityForUser(providerUser);

        Instructor instructor = new Instructor();
        instructor.setProvider(provider);
        applyRequest(instructor, request);
        return ProfileMapper.toResponse(instructorRepository.save(instructor));
    }

    public InstructorResponse update(User providerUser, Long id, InstructorRequest request) {
        Provider provider = providerService.getEntityForUser(providerUser);
        Instructor instructor = getEntity(id);
        requireOwnership(instructor, provider);

        applyRequest(instructor, request);
        return ProfileMapper.toResponse(instructorRepository.save(instructor));
    }

    public void delete(User providerUser, Long id) {
        Provider provider = providerService.getEntityForUser(providerUser);
        Instructor instructor = getEntity(id);
        requireOwnership(instructor, provider);
        instructorRepository.delete(instructor);
    }

    private void applyRequest(Instructor instructor, InstructorRequest request) {
        instructor.setName(request.getName());
        instructor.setCertification(request.getCertification());
        instructor.setExperience(request.getExperience());
        if (request.getAvailability() != null) {
            instructor.setAvailability(request.getAvailability());
        }
    }

    private void requireOwnership(Instructor instructor, Provider provider) {
        if (!instructor.getProvider().getProviderId().equals(provider.getProviderId())) {
            throw new ForbiddenActionException("This instructor does not belong to your provider account");
        }
    }
}
