package com.example.aquaventure.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.admin.RejectSchoolRequest;
import com.example.aquaventure.dto.admin.SchoolDetailDto;
import com.example.aquaventure.dto.admin.SchoolSummaryDto;
import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.common.PageResponse;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.service.SurfSchoolService;
import com.example.aquaventure.service.UserAdminService;
import com.example.aquaventure.security.UserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/schools")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminSchoolController {

    private final SurfSchoolService surfSchoolService;
    private final UserAdminService userAdminService;

    public AdminSchoolController(SurfSchoolService surfSchoolService, UserAdminService userAdminService) {
        this.surfSchoolService = surfSchoolService;
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public ApiResponse<PageResponse<SchoolSummaryDto>> list(
            @RequestParam(required = false) SchoolStatus status, Pageable pageable) {
        Page<SchoolSummaryDto> page = surfSchoolService.search(status, pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<SchoolDetailDto> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(surfSchoolService.getDetail(id));
    }

    @PatchMapping("/{id}/approve")
    public ApiResponse<SchoolDetailDto> approve(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal admin) {
        surfSchoolService.approve(id, userAdminService.getByIdOrThrow(admin.getId()));
        return ApiResponse.ok(surfSchoolService.getDetail(id));
    }

    @PatchMapping("/{id}/reject")
    public ApiResponse<SchoolDetailDto> reject(@PathVariable Long id, @Valid @RequestBody RejectSchoolRequest request) {
        surfSchoolService.reject(id, request.getReason());
        return ApiResponse.ok(surfSchoolService.getDetail(id));
    }

    @PatchMapping("/{id}/suspend")
    public ApiResponse<SchoolDetailDto> suspend(@PathVariable Long id) {
        surfSchoolService.suspend(id);
        return ApiResponse.ok(surfSchoolService.getDetail(id));
    }

    @PatchMapping("/{id}/reactivate")
    public ApiResponse<SchoolDetailDto> reactivate(@PathVariable Long id) {
        surfSchoolService.reactivate(id);
        return ApiResponse.ok(surfSchoolService.getDetail(id));
    }
}
