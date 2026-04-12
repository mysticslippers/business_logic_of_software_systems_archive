package me.ifmo.backend.repositories;

import me.ifmo.backend.entities.Enrollment;
import me.ifmo.backend.entities.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  List<Enrollment> findAllByUserId(Long userId);

  List<Enrollment> findAllByCourseId(Long courseId);

  boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, EnrollmentStatus status);

  List<Enrollment> findAllByStatusAndPaymentExpiresAtBefore(
          EnrollmentStatus status,
          LocalDateTime time
  );
}