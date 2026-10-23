package com.example.aquaventure.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.admin.UserSummaryDto;
import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.common.PageResponse;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.UserStatus;
import com.example.aquaventure.service.UserAdminService;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserSummaryDto>> list(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        Page<UserSummaryDto> page = userAdminService.search(role, status, q, pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @PatchMapping("/{id}/suspend")
    public ApiResponse<UserSummaryDto> suspend(@PathVariable Long id) {
        return ApiResponse.ok(userAdminService.suspend(id));
    }

    @PatchMapping("/{id}/reactivate")
    public ApiResponse<UserSummaryDto> reactivate(@PathVariable Long id) {
        return ApiResponse.ok(userAdminService.reactivate(id));
    }
}
