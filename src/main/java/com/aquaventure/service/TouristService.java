package com.aquaventure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.TouristRequest;
import com.aquaventure.dto.TouristResponse;
import com.aquaventure.entity.Role;
import com.aquaventure.entity.Tourist;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.mapper.ProfileMapper;
import com.aquaventure.repository.TouristRepository;

@Service
@Transactional
public class TouristService {

    @Autowired
    private TouristRepository touristRepository;

    public Tourist getEntity(Long id) {
        return touristRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found: " + id));
    }

    public Tourist getEntityForUser(User user) {
        return touristRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist profile not found"));
    }

    public TouristResponse get(Long id) {
        return ProfileMapper.toResponse(getEntity(id));
    }

    public TouristResponse update(User requester, Long id, TouristRequest request) {
        Tourist tourist = getEntity(id);
        requireOwnerOrAdmin(requester, tourist.getUser());

        if (request.getSkillLevel() != null) {
            tourist.setSkillLevel(request.getSkillLevel());
        }
        if (request.getExperience() != null) {
            tourist.setExperience(request.getExperience());
        }
        return ProfileMapper.toResponse(touristRepository.save(tourist));
    }

    private void requireOwnerOrAdmin(User requester, User owner) {
        if (!requester.getUserId().equals(owner.getUserId()) && requester.getRole() != Role.ADMIN) {
            throw new ForbiddenActionException("This profile does not belong to you");
        }
    }
}
