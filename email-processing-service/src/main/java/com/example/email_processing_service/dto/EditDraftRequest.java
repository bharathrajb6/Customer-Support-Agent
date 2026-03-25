package com.example.email_processing_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditDraftRequest {
    @NotBlank
    private String content;
}
