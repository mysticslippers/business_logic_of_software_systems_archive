package me.ifmo.backend.DTO.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer capacity;
    private Integer availablePlaces;
    private Boolean isActive;
}