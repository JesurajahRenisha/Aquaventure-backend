package com.example.aquaventure.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.common.PageResponse;
import com.example.aquaventure.dto.notification.NotificationDto;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.NotificationQueryService;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    public NotificationController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<NotificationDto>> listMine(@AuthenticationPrincipal UserPrincipal principal,
                                                                 Pageable pageable) {
        Page<NotificationDto> page = notificationQueryService.listForUser(principal.getId(), pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationDto> markRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.ok(notificationQueryService.markRead(principal.getId(), id));
    }

    @PatchMapping("/me/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationQueryService.markAllRead(principal.getId());
        return ApiResponse.ok(null);
    }
}
