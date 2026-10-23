package com.example.aquaventure.repository.spec;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonSlot;
import com.example.aquaventure.entity.LessonSlotStatus;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class SurfSchoolSpecifications {

    private SurfSchoolSpecifications() {
    }

    public static Specification<SurfSchool> isApproved() {
        return (root, query, cb) -> cb.equal(root.get("status"), SchoolStatus.APPROVED);
    }

    public static Specification<SurfSchool> hasLocation(Long locationId) {
        return (root, query, cb) -> {
            if (locationId == null) {
                return cb.conjunction();
            }
            Join<SurfSchool, SurfLocation> join = root.join("locations");
            return cb.equal(join.get("id"), locationId);
        };
    }

    /** Requires an active lesson matching every non-null filter. */
    public static Specification<SurfSchool> hasMatchingLesson(LessonType lessonType, SkillLevel skillLevel,
                                                                BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (lessonType == null && skillLevel == null && minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Lesson> lessonRoot = subquery.from(Lesson.class);
            subquery.select(lessonRoot.get("id"));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(lessonRoot.get("school"), root));
            predicates.add(cb.isTrue(lessonRoot.get("active")));
            if (lessonType != null) {
                predicates.add(cb.equal(lessonRoot.get("lessonType"), lessonType));
            }
            if (skillLevel != null) {
                predicates.add(cb.equal(lessonRoot.get("skillLevel"), skillLevel));
            }
            if (minPrice != null) {
                predicates.add(cb.ge(lessonRoot.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.le(lessonRoot.get("price"), maxPrice));
            }
            subquery.where(predicates.toArray(new Predicate[0]));
            return cb.exists(subquery);
        };
    }

    /** Requires at least one OPEN slot starting on the given date (school-local calendar day, treated as UTC). */
    public static Specification<SurfSchool> availableOn(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<LessonSlot> slotRoot = subquery.from(LessonSlot.class);
            subquery.select(slotRoot.get("id"));
            subquery.where(
                    cb.equal(slotRoot.get("lesson").get("school"), root),
                    cb.equal(slotRoot.get("status"), LessonSlotStatus.OPEN),
                    cb.greaterThanOrEqualTo(slotRoot.get("startTime"), from),
                    cb.lessThan(slotRoot.get("startTime"), to));
            return cb.exists(subquery);
        };
    }
}
