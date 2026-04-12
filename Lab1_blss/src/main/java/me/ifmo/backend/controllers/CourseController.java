package me.ifmo.backend.controllers;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.course.CourseDTO;
import me.ifmo.backend.mappers.CourseMapper;
import me.ifmo.backend.services.CourseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @GetMapping
    public List<CourseDTO> getAllCourses() {
        return courseMapper.toCourseDTOList(courseService.getAllCourses());
    }

    @GetMapping("/available")
    public List<CourseDTO> getAllAvailableCourses() {
        return courseMapper.toCourseDTOList(courseService.getAllAvailableCourses());
    }

    @GetMapping("/{id}")
    public CourseDTO getCourseById(@PathVariable Long id) {
        return courseMapper.toCourseDTO(courseService.getCourseById(id));
    }
}