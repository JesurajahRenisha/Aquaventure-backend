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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.SurfActivityRequest;
import com.aquaventure.dto.SurfActivityResponse;
import com.aquaventure.security.UserPrincipal;
import com.aquaventure.service.SurfActivityService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/activities")
@Tag(name = "Surf Activities")
public class SurfActivityController {

    @Autowired
    private SurfActivityService surfActivityService;

    @GetMapping
    public List<SurfActivityResponse> search(@RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long providerId) {
        return surfActivityService.search(locationId, providerId);
    }

    @GetMapping("/{id}")
    public SurfActivityResponse get(@PathVariable Long id) {
        return surfActivityService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    @ResponseStatus(HttpStatus.CREATED)
    public SurfActivityResponse create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SurfActivityRequest request) {
        return surfActivityService.create(principal.getUser(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public SurfActivityResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody SurfActivityRequest request) {
        return surfActivityService.update(principal.getUser(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        surfActivityService.delete(principal.getUser(), id);
    }
}
