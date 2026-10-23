package com.example.aquaventure.mapper;

import java.util.List;

import com.example.aquaventure.dto.school.EquipmentDto;
import com.example.aquaventure.entity.Equipment;

public final class EquipmentMapper {

    private EquipmentMapper() {
    }

    public static EquipmentDto toDto(Equipment equipment) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(equipment.getId());
        dto.setType(equipment.getType());
        dto.setName(equipment.getName());
        dto.setSize(equipment.getSize());
        dto.setTotalQuantity(equipment.getTotalQuantity());
        dto.setRentalPricePerHour(equipment.getRentalPricePerHour());
        dto.setRentalPricePerDay(equipment.getRentalPricePerDay());
        dto.setAvailable(equipment.isAvailable());
        return dto;
    }

    public static List<EquipmentDto> toDtoList(List<Equipment> equipmentList) {
        return equipmentList.stream().map(EquipmentMapper::toDto).toList();
    }
}
