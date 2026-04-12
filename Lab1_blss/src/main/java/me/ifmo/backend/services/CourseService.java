package me.ifmo.backend.services;

import me.ifmo.backend.entities.Course;

import java.util.List;

public interface CourseService {

    List<Course> getAllCourses();

    List<Course> getAllAvailableCourses();

    Course getCourseById(Long id);

    boolean isCourseAvailableForEnrollment(Long courseId);
}