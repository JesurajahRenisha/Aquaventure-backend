package com.aquaventure.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.InstructorRequest;
import com.aquaventure.dto.InstructorResponse;
import com.aquaventure.security.UserPrincipal;
import com.aquaventure.service.InstructorService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Instructors")
public class InstructorController {

    @Autowired
    private InstructorService instructorService;

    @GetMapping("/api/providers/{providerId}/instructors")
    public List<InstructorResponse> listForProvider(@PathVariable Long providerId) {
        return instructorService.listForProvider(providerId);
    }

    @GetMapping("/api/instructors/{id}")
    public InstructorResponse get(@PathVariable Long id) {
        return instructorService.get(id);
    }

    @PostMapping("/api/instructors")
    @PreAuthorize("hasRole('PROVIDER')")
    @ResponseStatus(HttpStatus.CREATED)
    public InstructorResponse create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InstructorRequest request) {
        return instructorService.create(principal.getUser(), request);
    }

    @PutMapping("/api/instructors/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public InstructorResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody InstructorRequest request) {
        return instructorService.update(principal.getUser(), id, request);
    }

    @DeleteMapping("/api/instructors/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        instructorService.delete(principal.getUser(), id);
    }
}
