package me.ifmo.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.enrollment.CreateEnrollmentRequest;
import me.ifmo.backend.DTO.enrollment.EnrollmentDTO;
import me.ifmo.backend.mappers.EnrollmentMapper;
import me.ifmo.backend.services.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentMapper enrollmentMapper;

    @PostMapping
    public EnrollmentDTO createEnrollment(@RequestBody @Valid CreateEnrollmentRequest request) {
        return enrollmentMapper.toEnrollmentDTO(
                enrollmentService.createEnrollment(request.getUserId(), request.getCourseId())
        );
    }

    @GetMapping("/{id}")
    public EnrollmentDTO getEnrollmentById(@PathVariable Long id) {
        return enrollmentMapper.toEnrollmentDTO(enrollmentService.getEnrollmentById(id));
    }

    @GetMapping("/user/{userId}")
    public List<EnrollmentDTO> getEnrollmentsByUserId(@PathVariable Long userId) {
        return enrollmentMapper.toEnrollmentDTOList(enrollmentService.getEnrollmentsByUserId(userId));
    }

    @GetMapping("/course/{courseId}")
    public List<EnrollmentDTO> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        return enrollmentMapper.toEnrollmentDTOList(enrollmentService.getEnrollmentsByCourseId(courseId));
    }
}