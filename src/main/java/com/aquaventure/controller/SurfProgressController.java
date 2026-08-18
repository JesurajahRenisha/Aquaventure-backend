package com.aquaventure.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.SurfProgressRequest;
import com.aquaventure.dto.SurfProgressResponse;
import com.aquaventure.security.UserPrincipal;
import com.aquaventure.service.SurfProgressService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/progress")
@Tag(name = "Surf Progress")
public class SurfProgressController {

    @Autowired
    private SurfProgressService surfProgressService;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public SurfProgressResponse create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SurfProgressRequest request) {
        return surfProgressService.create(principal.getUser(), request);
    }

    @GetMapping("/tourist/{touristId}")
    public List<SurfProgressResponse> listForTourist(@PathVariable Long touristId) {
        return surfProgressService.listForTourist(touristId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public SurfProgressResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody SurfProgressRequest request) {
        return surfProgressService.update(principal.getUser(), id, request);
    }
}
