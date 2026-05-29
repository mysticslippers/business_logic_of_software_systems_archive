package me.ifmo.backend.repositories;

import me.ifmo.backend.entities.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  Page<Enrollment> findAllByUserId(Long userId, Pageable pageable);

  Page<Enrollment> findAllByCourseId(Long courseId, Pageable pageable);
}