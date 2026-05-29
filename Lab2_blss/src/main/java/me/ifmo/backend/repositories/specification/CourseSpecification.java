package me.ifmo.backend.repositories.specification;

import me.ifmo.backend.entities.Course;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class CourseSpecification {

    private CourseSpecification() {}

    public static Specification<Course> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.isBlank()) {
                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<Course> hasMinPrice(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return null;
            }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Course> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return null;
            }

            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Course> hasActive(Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return null;
            }

            return criteriaBuilder.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<Course> onlyAvailable(Boolean onlyAvailable) {
        return (root, query, criteriaBuilder) -> {
            if (onlyAvailable == null || !onlyAvailable) {
                return null;
            }

            return criteriaBuilder.greaterThan(root.get("availablePlaces"), 0);
        };
    }
}