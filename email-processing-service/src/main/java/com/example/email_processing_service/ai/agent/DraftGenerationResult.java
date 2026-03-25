package com.example.email_processing_service.ai.agent;

import lombok.Data;

@Data
public class DraftGenerationResult {
    private String draft;
    private Double confidence;
}
