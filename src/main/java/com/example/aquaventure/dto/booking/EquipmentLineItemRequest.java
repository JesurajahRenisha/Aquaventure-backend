package com.example.aquaventure.dto.booking;

import com.example.aquaventure.entity.RentalUnit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class EquipmentLineItemRequest {

    @NotNull
    private Long equipmentId;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    private RentalUnit rentalUnit;

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public RentalUnit getRentalUnit() {
        return rentalUnit;
    }

    public void setRentalUnit(RentalUnit rentalUnit) {
        this.rentalUnit = rentalUnit;
    }
}
