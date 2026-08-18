package com.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.SurfActivityRequest;
import com.aquaventure.dto.SurfActivityResponse;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.SurfActivity;
import com.aquaventure.entity.SurfLocation;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.mapper.LocationActivityMapper;
import com.aquaventure.repository.SurfActivityRepository;

@Service
@Transactional
public class SurfActivityService {

    @Autowired
    private SurfActivityRepository surfActivityRepository;

    @Autowired
    private SurfLocationService surfLocationService;

    @Autowired
    private ProviderService providerService;

    public List<SurfActivityResponse> search(Long locationId, Long providerId) {
        List<SurfActivity> activities;
        if (locationId != null && providerId != null) {
            activities = surfActivityRepository.findByLocationAndProvider(
                    surfLocationService.getEntity(locationId), providerService.getEntity(providerId));
        } else if (locationId != null) {
            activities = surfActivityRepository.findByLocation(surfLocationService.getEntity(locationId));
        } else if (providerId != null) {
            activities = surfActivityRepository.findByProvider(providerService.getEntity(providerId));
        } else {
            activities = surfActivityRepository.findAll();
        }
        return activities.stream().map(LocationActivityMapper::toResponse).toList();
    }

    public SurfActivity getEntity(Long id) {
        return surfActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surf activity not found: " + id));
    }

    public SurfActivityResponse get(Long id) {
        return LocationActivityMapper.toResponse(getEntity(id));
    }

    public SurfActivityResponse create(User providerUser, SurfActivityRequest request) {
        Provider provider = providerService.getEntityForUser(providerUser);
        SurfLocation location = surfLocationService.getEntity(request.getLocationId());

        SurfActivity activity = new SurfActivity();
        activity.setProvider(provider);
        applyRequest(activity, location, request);
        return LocationActivityMapper.toResponse(surfActivityRepository.save(activity));
    }

    public SurfActivityResponse update(User providerUser, Long id, SurfActivityRequest request) {
        Provider provider = providerService.getEntityForUser(providerUser);
        SurfActivity activity = getEntity(id);
        requireOwnership(activity, provider);

        SurfLocation location = surfLocationService.getEntity(request.getLocationId());
        applyRequest(activity, location, request);
        return LocationActivityMapper.toResponse(surfActivityRepository.save(activity));
    }

    public void delete(User providerUser, Long id) {
        Provider provider = providerService.getEntityForUser(providerUser);
        SurfActivity activity = getEntity(id);
        requireOwnership(activity, provider);
        surfActivityRepository.delete(activity);
    }

    private void applyRequest(SurfActivity activity, SurfLocation location, SurfActivityRequest request) {
        activity.setLocation(location);
        activity.setActivityName(request.getActivityName());
        activity.setPrice(request.getPrice());
        activity.setDuration(request.getDuration());
        if (request.getActive() != null) {
            activity.setActive(request.getActive());
        }
    }

    private void requireOwnership(SurfActivity activity, Provider provider) {
        if (!activity.getProvider().getProviderId().equals(provider.getProviderId())) {
            throw new ForbiddenActionException("This activity does not belong to your provider account");
        }
    }
}
