package com.aquaventure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.Tourist;
import com.aquaventure.entity.User;

public interface TouristRepository extends JpaRepository<Tourist, Long> {

    Optional<Tourist> findByUser(User user);
}
