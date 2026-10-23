package com.example.aquaventure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aquaventure.entity.SurferProfile;

public interface SurferProfileRepository extends JpaRepository<SurferProfile, Long> {

    Optional<SurferProfile> findByUserId(Long userId);
}
