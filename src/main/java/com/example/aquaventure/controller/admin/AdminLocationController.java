package com.example.aquaventure.controller.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.location.LocationDto;
import com.example.aquaventure.dto.location.UpsertLocationRequest;
import com.example.aquaventure.service.SurfLocationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/locations")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminLocationController {

    private final SurfLocationService surfLocationService;

    public AdminLocationController(SurfLocationService surfLocationService) {
        this.surfLocationService = surfLocationService;
    }

    @GetMapping
    public ApiResponse<List<LocationDto>> list() {
        return ApiResponse.ok(surfLocationService.listAll());
    }

    @PostMapping
    public ApiResponse<LocationDto> create(@Valid @RequestBody UpsertLocationRequest request) {
        return ApiResponse.ok(surfLocationService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<LocationDto> update(@PathVariable Long id, @Valid @RequestBody UpsertLocationRequest request) {
        return ApiResponse.ok(surfLocationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        surfLocationService.softDelete(id);
        return ApiResponse.ok(null);
    }
}
