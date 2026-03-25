package com.example.email_processing_service.dto;

import com.example.email_processing_service.entity.DraftStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DraftResponse {
    private Long id;
    private Long emailId;
    private String content;
    private Double confidence;
    private DraftStatus status;
    private Instant createdAt;
}
