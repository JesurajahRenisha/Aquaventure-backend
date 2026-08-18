package com.aquaventure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.SurfLocation;

public interface SurfLocationRepository extends JpaRepository<SurfLocation, Long> {
}
