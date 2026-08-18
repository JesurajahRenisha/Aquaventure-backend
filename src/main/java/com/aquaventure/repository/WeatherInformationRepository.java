package com.aquaventure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.SurfLocation;
import com.aquaventure.entity.WeatherInformation;

public interface WeatherInformationRepository extends JpaRepository<WeatherInformation, Long> {

    Optional<WeatherInformation> findFirstByLocationOrderByRecordedAtDesc(SurfLocation location);
}
