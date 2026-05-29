package me.ifmo.backend.DTO.enrollment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEnrollmentRequest {

    @NotNull(message = "Course id must not be null")
    @Min(value = 1, message = "Course id must be greater than or equal to 1")
    private Long courseId;
}