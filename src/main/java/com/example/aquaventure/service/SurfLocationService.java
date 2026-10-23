package com.example.aquaventure.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.aquaventure.dto.location.LocationDto;
import com.example.aquaventure.dto.location.UpsertLocationRequest;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.SurfLocationMapper;
import com.example.aquaventure.repository.SurfLocationRepository;

@Service
public class SurfLocationService {

    private final SurfLocationRepository surfLocationRepository;

    public SurfLocationService(SurfLocationRepository surfLocationRepository) {
        this.surfLocationRepository = surfLocationRepository;
    }

    public List<LocationDto> listActive() {
        return surfLocationRepository.findByActiveTrue().stream().map(SurfLocationMapper::toDto).toList();
    }

    public List<LocationDto> listAll() {
        return surfLocationRepository.findAll().stream().map(SurfLocationMapper::toDto).toList();
    }

    public SurfLocation getByIdOrThrow(Long id) {
        return surfLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
    }

    public LocationDto getDto(Long id) {
        return SurfLocationMapper.toDto(getByIdOrThrow(id));
    }

    public LocationDto create(UpsertLocationRequest request) {
        SurfLocation location = new SurfLocation();
        apply(location, request);
        return SurfLocationMapper.toDto(surfLocationRepository.save(location));
    }

    public LocationDto update(Long id, UpsertLocationRequest request) {
        SurfLocation location = getByIdOrThrow(id);
        apply(location, request);
        return SurfLocationMapper.toDto(surfLocationRepository.save(location));
    }

    public void softDelete(Long id) {
        SurfLocation location = getByIdOrThrow(id);
        location.setActive(false);
        surfLocationRepository.save(location);
    }

    private void apply(SurfLocation location, UpsertLocationRequest request) {
        location.setName(request.getName());
        location.setRegion(request.getRegion());
        location.setDescription(request.getDescription());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setActive(request.isActive());
    }
}
