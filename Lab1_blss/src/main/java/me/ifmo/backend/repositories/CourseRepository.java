package me.ifmo.backend.repositories;

import me.ifmo.backend.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByIsActiveTrue();

    @Query("""
            SELECT course
            FROM Course course
            WHERE course.isActive = TRUE
              AND course.availablePlaces > 0
            """)
    List<Course> findAllAvailableCourses();
}