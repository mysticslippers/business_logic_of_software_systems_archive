package me.ifmo.backend.services;

import me.ifmo.backend.entities.Enrollment;
import me.ifmo.backend.entities.enums.EnrollmentStatus;

import java.util.List;

public interface EnrollmentService {

    Enrollment createEnrollment(Long userId, Long courseId);

    Enrollment getEnrollmentById(Long id);

    List<Enrollment> getEnrollmentsByUserId(Long userId);

    List<Enrollment> getEnrollmentsByCourseId(Long courseId);

    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, EnrollmentStatus status);
}