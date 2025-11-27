package com.groupe.gestion_.de_.notes.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class GradeRequest {

    @NotNull(message = "Grade value cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Grade value must be 0.0 or greater")
    @DecimalMax(value = "100.0", message = "Grade value must be 100.0 or smaller")
    private Double value;

    @NotNull(message = "Grade date cannot be null")
    @PastOrPresent(message = "Grade date cannot be in the future")
    private LocalDate date;

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment; // Optional, hence no @NotBlank

    @NotNull(message = "Student ID cannot be null")
    private String studentIdNum; // ID of the associated Student

    @NotNull(message = "Subject ID cannot be null")
    private String subjectCode; // ID of the associated Subject

    // Optional: Teacher who recorded the grade. Can be null if not required.
    // If it's mandatory, add @NotNull
    private String recordedBy;
}
