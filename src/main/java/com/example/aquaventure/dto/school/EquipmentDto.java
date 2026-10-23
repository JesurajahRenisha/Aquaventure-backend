package com.example.aquaventure.dto.school;

import java.math.BigDecimal;

import com.example.aquaventure.entity.EquipmentType;

public class EquipmentDto {

    private Long id;
    private EquipmentType type;
    private String name;
    private String size;
    private Integer totalQuantity;
    private BigDecimal rentalPricePerHour;
    private BigDecimal rentalPricePerDay;
    private boolean available;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
