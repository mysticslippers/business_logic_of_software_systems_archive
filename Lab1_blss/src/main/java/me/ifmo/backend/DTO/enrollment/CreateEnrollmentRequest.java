package me.ifmo.backend.DTO.enrollment;

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

    @NotNull(message = "User id must not be null")
    private Long userId;

    @NotNull(message = "Course id must not be null")
    private Long courseId;
}