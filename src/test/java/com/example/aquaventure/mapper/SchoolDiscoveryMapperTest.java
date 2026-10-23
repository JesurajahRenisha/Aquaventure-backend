package com.example.aquaventure.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.aquaventure.dto.discovery.SchoolDetailPublicDto;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

class SchoolDiscoveryMapperTest {

    @Test
    void toDetailPublicDto_neverLeaksSensitiveFields() throws Exception {
        User owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        SurfSchool school = new SurfSchool();
        school.setId(1L);
        school.setName("Arugam Surf Co");
        school.setOwner(owner);
        school.setStatus(SchoolStatus.APPROVED);
        school.setBusinessRegistrationNumber("TOP-SECRET-BRN");
        school.setRejectionReason("should never be visible");
        school.setVerificationDocumentUrls(List.of("school-documents/owner/passport.pdf"));

        SchoolDetailPublicDto dto = SchoolDiscoveryMapper.toDetailPublicDto(school, List.of(), List.of(), List.of());

        String json = new ObjectMapper().writeValueAsString(dto);

        assertThat(json).doesNotContain("TOP-SECRET-BRN", "should never be visible", "passport.pdf",
                "businessRegistrationNumber", "rejectionReason", "verificationDocumentUrls");
    }
}
