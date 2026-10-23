package com.example.aquaventure.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.exception.OwnershipViolationException;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.SurfSchoolService;
import com.example.aquaventure.service.storage.FileStorageService;

/**
 * Verification documents are sensitive: never served as static resources. Only the
 * platform admin or the owning school admin may stream them, and the stored path is
 * always resolved server-side from the school's own document list - never from client input.
 */
@RestController
@RequestMapping("/api/schools/{schoolId}/documents")
@PreAuthorize("isAuthenticated()")
public class SchoolDocumentController {

    private final SurfSchoolService surfSchoolService;
    private final FileStorageService fileStorageService;

    public SchoolDocumentController(SurfSchoolService surfSchoolService, FileStorageService fileStorageService) {
        this.surfSchoolService = surfSchoolService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{index}")
    public ResponseEntity<Resource> getDocument(@PathVariable Long schoolId, @PathVariable int index,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        SurfSchool school = surfSchoolService.getByIdOrThrow(schoolId);

        boolean isPlatformAdmin = principal.getRole() == Role.PLATFORM_ADMIN;
        boolean isOwner = school.getOwner() != null && school.getOwner().getId().equals(principal.getId());
        if (!isPlatformAdmin && !isOwner) {
            throw new OwnershipViolationException("You may not access this school's documents");
        }

        if (index < 0 || index >= school.getVerificationDocumentUrls().size()) {
            throw new ResourceNotFoundException("Document not found");
        }

        String storedPath = school.getVerificationDocumentUrls().get(index);
        Resource resource = fileStorageService.load(storedPath);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }
}
