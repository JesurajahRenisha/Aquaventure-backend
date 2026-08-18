package com.aquaventure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.Instructor;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.User;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    List<Instructor> findByProvider(Provider provider);

    Optional<Instructor> findByUser(User user);
}
