package com.example.email_processing_service.ai.agent;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class IntentAnalysisResult {
    private String intent;
    private Map<String, String> entities;
    private List<String> tools;
    private Double confidence;
}
