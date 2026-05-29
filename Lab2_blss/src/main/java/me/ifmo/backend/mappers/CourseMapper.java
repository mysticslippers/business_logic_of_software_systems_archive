package me.ifmo.backend.mappers;

import me.ifmo.backend.DTO.course.CourseDTO;
import me.ifmo.backend.entities.Course;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseDTO toCourseDTO(Course course);

    List<CourseDTO> toCourseDTOList(List<Course> courses);
}
