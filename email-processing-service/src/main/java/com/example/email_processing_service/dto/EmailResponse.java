package com.example.email_processing_service.dto;

import com.example.email_processing_service.entity.EmailStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class EmailResponse {
    private Long id;
    private String gmailId;
    private String threadId;
    private String sender;
    private String subject;
    private String body;
    private EmailStatus status;
    private Instant createdAt;
}
