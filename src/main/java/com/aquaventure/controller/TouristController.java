package com.aquaventure.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.TouristRequest;
import com.aquaventure.dto.TouristResponse;
import com.aquaventure.security.UserPrincipal;
import com.aquaventure.service.TouristService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tourists")
@Tag(name = "Profile - Tourist")
public class TouristController {

    @Autowired
    private TouristService touristService;

    @GetMapping("/{id}")
    public TouristResponse get(@PathVariable Long id) {
        return touristService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SURFER', 'ADMIN')")
    public TouristResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody TouristRequest request) {
        return touristService.update(principal.getUser(), id, request);
    }
}
