package com.example.aquaventure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aquaventure.entity.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Page<Equipment> findBySchoolId(Long schoolId, Pageable pageable);

    List<Equipment> findBySchoolIdAndAvailableTrue(Long schoolId);

    Optional<Equipment> findByIdAndSchoolId(Long id, Long schoolId);
}
