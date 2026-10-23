package com.example.aquaventure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.SchoolStatus;

public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {

    Page<Lesson> findBySchoolId(Long schoolId, Pageable pageable);

    Optional<Lesson> findByIdAndSchoolId(Long id, Long schoolId);

    List<Lesson> findBySchoolIdAndActiveTrue(Long schoolId);

    Optional<Lesson> findByIdAndActiveTrueAndSchoolStatus(Long id, SchoolStatus status);
}
