package com.example.aquaventure.controller.school;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.school.SchoolDashboardDto;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.SchoolDashboardService;

@RestController
@RequestMapping("/api/school/me/dashboard")
@PreAuthorize("hasRole('SCHOOL_ADMIN')")
public class SchoolDashboardController {

    private final SchoolDashboardService schoolDashboardService;

    public SchoolDashboardController(SchoolDashboardService schoolDashboardService) {
        this.schoolDashboardService = schoolDashboardService;
    }

    @GetMapping
    public ApiResponse<SchoolDashboardDto> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(schoolDashboardService.getDashboard(principal.getId()));
    }
}
