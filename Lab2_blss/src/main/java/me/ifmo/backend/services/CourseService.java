package me.ifmo.backend.services;

import me.ifmo.backend.entities.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CourseService {

    Page<Course> getCourses(
            String title,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Boolean onlyAvailable,
            Pageable pageable
    );

    Course getCourseById(Long id);

    boolean isCourseAvailableForEnrollment(Long courseId);
}