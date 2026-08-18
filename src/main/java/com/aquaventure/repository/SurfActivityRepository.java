package com.aquaventure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.Provider;
import com.aquaventure.entity.SurfActivity;
import com.aquaventure.entity.SurfLocation;

public interface SurfActivityRepository extends JpaRepository<SurfActivity, Long> {

    List<SurfActivity> findByLocation(SurfLocation location);

    List<SurfActivity> findByProvider(Provider provider);

    List<SurfActivity> findByLocationAndProvider(SurfLocation location, Provider provider);
}
