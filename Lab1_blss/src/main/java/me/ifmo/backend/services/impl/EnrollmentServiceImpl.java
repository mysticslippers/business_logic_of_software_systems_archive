package me.ifmo.backend.services.impl;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.entities.Course;
import me.ifmo.backend.entities.Enrollment;
import me.ifmo.backend.entities.User;
import me.ifmo.backend.entities.enums.EnrollmentStatus;
import me.ifmo.backend.exceptions.BusinessException;
import me.ifmo.backend.exceptions.NotFoundException;
import me.ifmo.backend.repositories.EnrollmentRepository;
import me.ifmo.backend.repositories.UserRepository;
import me.ifmo.backend.services.CourseService;
import me.ifmo.backend.services.EnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;

    @Override
    public Enrollment createEnrollment(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        Course course = courseService.getCourseById(courseId);

        if (!courseService.isCourseAvailableForEnrollment(courseId)) {
            throw new BusinessException("Course with id " + courseId + " is not available for enrollment");
        }

        LocalDateTime now = LocalDateTime.now();

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.PENDING_PAYMENT)
                .createdAt(now)
                .updatedAt(now)
                .paymentExpiresAt(now.plusMinutes(15))
                .build();

        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Enrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByUserId(Long userId) {
        return enrollmentRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findAllByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, EnrollmentStatus status) {
        return enrollmentRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, status);
    }
}