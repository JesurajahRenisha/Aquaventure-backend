package com.example.aquaventure.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonSlotStatus;

public interface LessonSlotRepository extends JpaRepository<LessonSlot, Long> {

    Page<LessonSlot> findByLessonSchoolId(Long schoolId, Pageable pageable);

    Optional<LessonSlot> findByIdAndLessonSchoolId(Long id, Long schoolId);

    List<LessonSlot> findByLessonIdAndStatusAndStartTimeBetween(Long lessonId, LessonSlotStatus status,
                                                                 Instant from, Instant to);

    List<LessonSlot> findByLessonSchoolIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
            Long schoolId, LessonSlotStatus status, Instant after, org.springframework.data.domain.Pageable pageable);

    @Query("""
            select (count(s) > 0) from LessonSlot s
            where s.instructor.id = :instructorId
              and s.status <> com.example.aquaventure.entity.LessonSlotStatus.CANCELLED
              and (:excludeId is null or s.id <> :excludeId)
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    boolean existsOverlap(@Param("instructorId") Long instructorId,
                           @Param("startTime") Instant startTime,
                           @Param("endTime") Instant endTime,
                           @Param("excludeId") Long excludeId);
}
