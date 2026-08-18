package com.aquaventure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.SurfProgress;
import com.aquaventure.entity.Tourist;

public interface SurfProgressRepository extends JpaRepository<SurfProgress, Long> {

    List<SurfProgress> findByTouristOrderBySessionDateDesc(Tourist tourist);
}
