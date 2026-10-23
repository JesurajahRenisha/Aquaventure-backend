package com.example.aquaventure.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.admin.AdminStatsDto;
import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.service.AdminStatsService;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping
    public ApiResponse<AdminStatsDto> getStats() {
        return ApiResponse.ok(adminStatsService.getStats());
    }
}
