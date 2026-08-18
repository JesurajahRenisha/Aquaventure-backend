package com.aquaventure.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.EquipmentRequest;
import com.aquaventure.dto.EquipmentResponse;
import com.aquaventure.security.UserPrincipal;
import com.aquaventure.service.EquipmentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping("/api/providers/{providerId}/equipment")
    public List<EquipmentResponse> listForProvider(@PathVariable Long providerId) {
        return equipmentService.listForProvider(providerId);
    }

    @PostMapping("/api/equipment")
    @PreAuthorize("hasRole('PROVIDER')")
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.create(principal.getUser(), request);
    }

    @PutMapping("/api/equipment/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public EquipmentResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.update(principal.getUser(), id, request);
    }

    @DeleteMapping("/api/equipment/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        equipmentService.delete(principal.getUser(), id);
    }
}
