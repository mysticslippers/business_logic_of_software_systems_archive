package me.ifmo.backend.services.impl;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.entities.Course;
import me.ifmo.backend.exceptions.NotFoundException;
import me.ifmo.backend.repositories.CourseRepository;
import me.ifmo.backend.services.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAllByIsActiveTrue();
    }

    @Override
    public List<Course> getAllAvailableCourses() {
        return courseRepository.findAllAvailableCourses();
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