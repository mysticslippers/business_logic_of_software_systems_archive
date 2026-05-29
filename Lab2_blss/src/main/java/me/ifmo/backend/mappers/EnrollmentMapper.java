package me.ifmo.backend.mappers;

import me.ifmo.backend.DTO.enrollment.EnrollmentDTO;
import me.ifmo.backend.entities.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "courseId", source = "course.id")
    EnrollmentDTO toEnrollmentDTO(Enrollment enrollment);

    List<EnrollmentDTO> toEnrollmentDTOList(List<Enrollment> enrollments);
}