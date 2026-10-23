package com.example.aquaventure.controller.discovery;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.location.SurfConditionDto;
import com.example.aquaventure.service.SurfConditionService;

@RestController
@RequestMapping("/api/locations")
public class ConditionController {

    private final SurfConditionService surfConditionService;

    public ConditionController(SurfConditionService surfConditionService) {
        this.surfConditionService = surfConditionService;
    }

    @GetMapping("/{id}/conditions")
    public ApiResponse<SurfConditionDto> getConditions(@PathVariable Long id) {
        return ApiResponse.ok(surfConditionService.getConditions(id));
    }
}
