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

import com.aquaventure.dto.ProviderRequest;
import com.aquaventure.dto.ProviderResponse;
import com.aquaventure.security.UserPrincipal;
import com.aquaventure.service.ProviderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/providers")
@Tag(name = "Profile - Provider")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @GetMapping("/{id}")
    public ProviderResponse get(@PathVariable Long id) {
        return providerService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ProviderResponse update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody ProviderRequest request) {
        return providerService.update(principal.getUser(), id, request);
    }
}
