package me.ifmo.backend.services;

import me.ifmo.backend.entities.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {

    Enrollment createEnrollment(String userEmail, Long courseId);

    Enrollment createEnrollmentForUser(Long userId, Long courseId);

    Enrollment getEnrollmentById(Long id);

    Page<Enrollment> getEnrollmentsByUserId(Long userId, Pageable pageable);

    Page<Enrollment> getEnrollmentsByCourseId(Long courseId, Pageable pageable);
}