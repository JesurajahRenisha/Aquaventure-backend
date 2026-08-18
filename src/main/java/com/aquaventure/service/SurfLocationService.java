package com.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.SurfLocationRequest;
import com.aquaventure.dto.SurfLocationResponse;
import com.aquaventure.entity.SurfLocation;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.mapper.LocationActivityMapper;
import com.aquaventure.repository.SurfLocationRepository;

@Service
@Transactional
public class SurfLocationService {

    @Autowired
    private SurfLocationRepository surfLocationRepository;

    public List<SurfLocationResponse> listAll() {
        return surfLocationRepository.findAll().stream().map(LocationActivityMapper::toResponse).toList();
    }

    public SurfLocation getEntity(Long id) {
        return surfLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surf location not found: " + id));
    }

    public SurfLocationResponse get(Long id) {
        return LocationActivityMapper.toResponse(getEntity(id));
    }

    public SurfLocationResponse create(SurfLocationRequest request) {
        SurfLocation location = new SurfLocation();
        applyRequest(location, request);
        return LocationActivityMapper.toResponse(surfLocationRepository.save(location));
    }

    public SurfLocationResponse update(Long id, SurfLocationRequest request) {
        SurfLocation location = getEntity(id);
        applyRequest(location, request);
        return LocationActivityMapper.toResponse(surfLocationRepository.save(location));
    }

    public void delete(Long id) {
        surfLocationRepository.delete(getEntity(id));
    }

    private void applyRequest(SurfLocation location, SurfLocationRequest request) {
        location.setLocationName(request.getLocationName());
        location.setDifficultyLevel(request.getDifficultyLevel());
        location.setSafetyRating(request.getSafetyRating());
    }
}
