package com.example.aquaventure.controller.surfer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.common.PageResponse;
import com.example.aquaventure.dto.surfer.ProgressEntryDto;
import com.example.aquaventure.dto.surfer.ProgressSummaryDto;
import com.example.aquaventure.dto.surfer.UpsertProgressEntryRequest;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.ProgressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/surfer/me/progress")
@PreAuthorize("hasRole('SURFER')")
public class SurferProgressController {

    private final ProgressService progressService;

    public SurferProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProgressEntryDto>> list(@AuthenticationPrincipal UserPrincipal principal, Pageable pageable) {
        Page<ProgressEntryDto> page = progressService.list(principal.getId(), pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @GetMapping("/summary")
    public ApiResponse<ProgressSummaryDto> summary(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(progressService.summary(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProgressEntryDto>> create(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody UpsertProgressEntryRequest request) {
        ProgressEntryDto dto = progressService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProgressEntryDto> update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                                 @Valid @RequestBody UpsertProgressEntryRequest request) {
        return ApiResponse.ok(progressService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        progressService.delete(principal.getId(), id);
        return ApiResponse.ok(null);
    }
}
