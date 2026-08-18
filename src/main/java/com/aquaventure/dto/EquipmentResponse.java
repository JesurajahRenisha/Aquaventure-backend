package com.aquaventure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EquipmentResponse {
    private Long equipmentId;
    private Long providerId;
    private String equipmentName;
    private Integer quantity;
    private boolean availability;
}
