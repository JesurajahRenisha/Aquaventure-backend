package com.example.aquaventure.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.school.EquipmentDto;
import com.example.aquaventure.dto.school.UpsertEquipmentRequest;
import com.example.aquaventure.entity.Equipment;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.EquipmentMapper;
import com.example.aquaventure.repository.EquipmentRepository;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final SurfSchoolService surfSchoolService;

    public EquipmentService(EquipmentRepository equipmentRepository, SurfSchoolService surfSchoolService) {
        this.equipmentRepository = equipmentRepository;
        this.surfSchoolService = surfSchoolService;
    }

    @Transactional(readOnly = true)
    public Page<EquipmentDto> list(Long ownerId, Pageable pageable) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);
        return equipmentRepository.findBySchoolId(school.getId(), pageable).map(EquipmentMapper::toDto);
    }

    @Transactional
    public EquipmentDto create(Long ownerId, UpsertEquipmentRequest request) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Equipment equipment = new Equipment();
        equipment.setSchool(school);
        apply(equipment, request);
        return EquipmentMapper.toDto(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentDto update(Long ownerId, Long equipmentId, UpsertEquipmentRequest request) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Equipment equipment = getOwnedOrThrow(equipmentId, school.getId());
        apply(equipment, request);
        return EquipmentMapper.toDto(equipmentRepository.save(equipment));
    }

    @Transactional
    public void delete(Long ownerId, Long equipmentId) {
        SurfSchool school = surfSchoolService.getOwnedApprovedSchoolOrThrow(ownerId);
        Equipment equipment = getOwnedOrThrow(equipmentId, school.getId());
        equipmentRepository.delete(equipment);
    }

    private Equipment getOwnedOrThrow(Long equipmentId, Long schoolId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + equipmentId));
        if (!equipment.getSchool().getId().equals(schoolId)) {
            throw new OwnershipViolationException("This equipment does not belong to your school");
        }
        return equipment;
    }

    private void apply(Equipment equipment, UpsertEquipmentRequest request) {
        equipment.setType(request.getType());
        equipment.setName(request.getName());
        equipment.setSize(request.getSize());
        equipment.setTotalQuantity(request.getTotalQuantity());
        equipment.setRentalPricePerHour(request.getRentalPricePerHour());
        equipment.setRentalPricePerDay(request.getRentalPricePerDay());
        equipment.setAvailable(request.isAvailable());
    }
}
