package com.example.aquaventure.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SurfSchool;

public interface SurfSchoolRepository extends JpaRepository<SurfSchool, Long>, JpaSpecificationExecutor<SurfSchool> {

    Optional<SurfSchool> findByOwnerId(Long ownerId);

    Page<SurfSchool> findByStatus(SchoolStatus status, Pageable pageable);

    long countByStatus(SchoolStatus status);
}
