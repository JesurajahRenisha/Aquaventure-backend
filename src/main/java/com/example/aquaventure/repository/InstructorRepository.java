package com.example.aquaventure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aquaventure.entity.Instructor;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Page<Instructor> findBySchoolId(Long schoolId, Pageable pageable);

    List<Instructor> findBySchoolIdAndActiveTrue(Long schoolId);

    Optional<Instructor> findByIdAndSchoolId(Long id, Long schoolId);
}
