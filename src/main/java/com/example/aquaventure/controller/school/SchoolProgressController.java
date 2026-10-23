package com.example.aquaventure.controller.school;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.school.InstructorFeedbackRequest;
import com.example.aquaventure.dto.surfer.ProgressEntryDto;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.ProgressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/school/me/progress")
@PreAuthorize("hasRole('SCHOOL_ADMIN')")
public class SchoolProgressController {

    private final ProgressService progressService;

    public SchoolProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PatchMapping("/{id}/feedback")
    public ApiResponse<ProgressEntryDto> addFeedback(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody InstructorFeedbackRequest request) {
        return ApiResponse.ok(progressService.addInstructorFeedback(principal.getId(), id, request.getFeedback()));
    }
}
