package com.example.aquaventure.dto.school;

import java.math.BigDecimal;

import com.example.aquaventure.entity.EquipmentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class UpsertEquipmentRequest {

    @NotNull
    private EquipmentType type;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 50)
    private String size;

    @NotNull
    @PositiveOrZero
    private Integer totalQuantity;

    @NotNull
    @Positive
    private BigDecimal rentalPricePerHour;

    @NotNull
    @Positive
    private BigDecimal rentalPricePerDay;

    private boolean available = true;

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getRentalPricePerHour() {
        return rentalPricePerHour;
    }

    public void setRentalPricePerHour(BigDecimal rentalPricePerHour) {
        this.rentalPricePerHour = rentalPricePerHour;
    }

    public BigDecimal getRentalPricePerDay() {
        return rentalPricePerDay;
    }

    public void setRentalPricePerDay(BigDecimal rentalPricePerDay) {
        this.rentalPricePerDay = rentalPricePerDay;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
