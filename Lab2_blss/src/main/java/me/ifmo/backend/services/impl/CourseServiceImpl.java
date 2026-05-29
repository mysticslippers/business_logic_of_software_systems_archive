package me.ifmo.backend.services.impl;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.entities.Course;
import me.ifmo.backend.exceptions.BusinessException;
import me.ifmo.backend.exceptions.NotFoundException;
import me.ifmo.backend.repositories.CourseRepository;
import me.ifmo.backend.repositories.specification.CourseSpecification;
import me.ifmo.backend.services.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public Page<Course> getCourses(
            String title,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Boolean onlyAvailable,
            Pageable pageable
    ) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("minPrice must be less than or equal to maxPrice");
        }

        Specification<Course> specification = Specification.allOf(
                CourseSpecification.hasTitle(title),
                CourseSpecification.hasMinPrice(minPrice),
                CourseSpecification.hasMaxPrice(maxPrice),
                CourseSpecification.hasActive(isActive),
                CourseSpecification.onlyAvailable(onlyAvailable)
        );

        return courseRepository.findAll(specification, pageable);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course with id " + id + " not found"));
    }

    @Override
    public boolean isCourseAvailableForEnrollment(Long courseId) {
        Course course = getCourseById(courseId);

        return Boolean.TRUE.equals(course.getIsActive())
                && course.getAvailablePlaces() != null
                && course.getAvailablePlaces() > 0;
    }
}