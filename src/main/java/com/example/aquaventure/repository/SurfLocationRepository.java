package com.example.aquaventure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aquaventure.entity.SurfLocation;

public interface SurfLocationRepository extends JpaRepository<SurfLocation, Long> {

    List<SurfLocation> findByActiveTrue();
}
