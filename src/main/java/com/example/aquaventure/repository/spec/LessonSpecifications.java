package com.example.aquaventure.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;

public final class LessonSpecifications {

    private LessonSpecifications() {
    }

    public static Specification<Lesson> isActiveAtApprovedSchool() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("active")),
                cb.equal(root.get("school").get("status"), SchoolStatus.APPROVED));
    }

    public static Specification<Lesson> hasSkillLevel(SkillLevel skillLevel) {
        return (root, query, cb) -> skillLevel == null
                ? cb.conjunction()
                : cb.equal(root.get("skillLevel"), skillLevel);
    }

    public static Specification<Lesson> atLocation(Long locationId) {
        return (root, query, cb) -> locationId == null
                ? cb.conjunction()
                : cb.equal(root.get("location").get("id"), locationId);
    }
}
