package com.example.aquaventure.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.aquaventure.entity.Booking;
import com.example.aquaventure.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findBySurferId(Long surferId, Pageable pageable);

    Page<Booking> findBySurferIdAndStatus(Long surferId, BookingStatus status, Pageable pageable);

    Page<Booking> findBySchoolId(Long schoolId, Pageable pageable);

    Page<Booking> findBySchoolIdAndStatus(Long schoolId, BookingStatus status, Pageable pageable);

    Optional<Booking> findByIdAndSurferId(Long id, Long surferId);

    Optional<Booking> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, BookingStatus status);

    long countByStatus(BookingStatus status);

    @Query("""
            select coalesce(s.location.name, l.location.name, 'Unknown'), count(b)
            from Booking b
            join b.lessonSlot s
            join s.lesson l
            group by coalesce(s.location.name, l.location.name, 'Unknown')
            """)
    List<Object[]> countGroupedByLocation();

    @Query("select coalesce(sum(b.totalPrice), 0) from Booking b where b.school.id = :schoolId and b.status = 'COMPLETED'")
    BigDecimal sumRevenueForSchool(@Param("schoolId") Long schoolId);

    @Query("""
            select coalesce(sum(b.totalPrice), 0) from Booking b
            where b.school.id = :schoolId and b.status = 'COMPLETED' and b.completedAt >= :from
            """)
    BigDecimal sumRevenueForSchoolSince(@Param("schoolId") Long schoolId, @Param("from") Instant from);
}
