package com.example.aquaventure.controller.discovery;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.location.LocationDto;
import com.example.aquaventure.service.SurfLocationService;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final SurfLocationService surfLocationService;

    public LocationController(SurfLocationService surfLocationService) {
        this.surfLocationService = surfLocationService;
    }

    @GetMapping
    public ApiResponse<List<LocationDto>> list() {
        return ApiResponse.ok(surfLocationService.listActive());
    }
}
