package com.aquaventure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.Equipment;
import com.aquaventure.entity.Provider;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByProvider(Provider provider);
}
