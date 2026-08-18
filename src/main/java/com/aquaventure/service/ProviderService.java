package com.aquaventure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.ProviderRequest;
import com.aquaventure.dto.ProviderResponse;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.Role;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.mapper.ProfileMapper;
import com.aquaventure.repository.ProviderRepository;

@Service
@Transactional
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    public Provider getEntity(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + id));
    }

    public Provider getEntityForUser(User user) {
        return providerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));
    }

    public ProviderResponse get(Long id) {
        return ProfileMapper.toResponse(getEntity(id));
    }

    public ProviderResponse update(User requester, Long id, ProviderRequest request) {
        Provider provider = getEntity(id);
        requireOwnerOrAdmin(requester, provider.getUser());

        if (request.getBusinessName() != null) {
            provider.setBusinessName(request.getBusinessName());
        }
        if (request.getContactDetails() != null) {
            provider.setContactDetails(request.getContactDetails());
        }
        if (request.getLocation() != null) {
            provider.setLocation(request.getLocation());
        }
        return ProfileMapper.toResponse(providerRepository.save(provider));
    }

    private void requireOwnerOrAdmin(User requester, User owner) {
        if (!requester.getUserId().equals(owner.getUserId()) && requester.getRole() != Role.ADMIN) {
            throw new ForbiddenActionException("This profile does not belong to you");
        }
    }
}
