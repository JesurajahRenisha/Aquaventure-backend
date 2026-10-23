package com.example.aquaventure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aquaventure.entity.ProgressEntry;

public interface ProgressEntryRepository extends JpaRepository<ProgressEntry, Long> {

    Page<ProgressEntry> findBySurferId(Long surferId, Pageable pageable);

    List<ProgressEntry> findBySurferId(Long surferId);

    Optional<ProgressEntry> findByIdAndSurferId(Long id, Long surferId);

    Optional<ProgressEntry> findByBookingId(Long bookingId);
}
