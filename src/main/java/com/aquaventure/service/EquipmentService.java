package com.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.EquipmentRequest;
import com.aquaventure.dto.EquipmentResponse;
import com.aquaventure.entity.Equipment;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.repository.EquipmentRepository;

@Service
@Transactional
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private ProviderService providerService;

    public List<EquipmentResponse> listForProvider(Long providerId) {
        Provider provider = providerService.getEntity(providerId);
        return equipmentRepository.findByProvider(provider).stream().map(this::toResponse).toList();
    }

    public Equipment getEntity(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + id));
    }

    public EquipmentResponse create(User providerUser, EquipmentRequest request) {
        Provider provider = providerService.getEntityForUser(providerUser);

        Equipment equipment = new Equipment();
        equipment.setProvider(provider);
        applyRequest(equipment, request);
        return toResponse(equipmentRepository.save(equipment));
    }

    public EquipmentResponse update(User providerUser, Long id, EquipmentRequest request) {
        Provider provider = providerService.getEntityForUser(providerUser);
        Equipment equipment = getEntity(id);
        requireOwnership(equipment, provider);

        applyRequest(equipment, request);
        return toResponse(equipmentRepository.save(equipment));
    }

    public void delete(User providerUser, Long id) {
        Provider provider = providerService.getEntityForUser(providerUser);
        Equipment equipment = getEntity(id);
        requireOwnership(equipment, provider);
        equipmentRepository.delete(equipment);
    }

    private void applyRequest(Equipment equipment, EquipmentRequest request) {
        equipment.setEquipmentName(request.getEquipmentName());
        equipment.setQuantity(request.getQuantity());
        if (request.getAvailability() != null) {
            equipment.setAvailability(request.getAvailability());
        }
    }

    private void requireOwnership(Equipment equipment, Provider provider) {
        if (!equipment.getProvider().getProviderId().equals(provider.getProviderId())) {
            throw new ForbiddenActionException("This equipment does not belong to your provider account");
        }
    }

    private EquipmentResponse toResponse(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getEquipmentId(),
                equipment.getProvider().getProviderId(),
                equipment.getEquipmentName(),
                equipment.getQuantity(),
                equipment.isAvailability());
    }
}
