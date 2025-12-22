package com.groupe.gestion_.de_.notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SubjectRequest {
    @NotBlank(message = "Subject code is required")
    private String subjectCode;
    
    @NotBlank(message = "Subject name is required")
    private String name;
    
    @NotNull(message = "Coefficient is required")
    @Positive(message = "Coefficient must be positive")
    private Double coefficient;
    
    @NotBlank(message = "Description is required")
    private String description;

}
