package com.aquaventure.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.SurfLocationRequest;
import com.aquaventure.dto.SurfLocationResponse;
import com.aquaventure.service.SurfLocationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Surf Locations")
public class SurfLocationController {

    @Autowired
    private SurfLocationService surfLocationService;

    @GetMapping
    public List<SurfLocationResponse> list() {
        return surfLocationService.listAll();
    }

    @GetMapping("/{id}")
    public SurfLocationResponse get(@PathVariable Long id) {
        return surfLocationService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SurfLocationResponse create(@Valid @RequestBody SurfLocationRequest request) {
        return surfLocationService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public SurfLocationResponse update(@PathVariable Long id, @Valid @RequestBody SurfLocationRequest request) {
        return surfLocationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        surfLocationService.delete(id);
    }
}
