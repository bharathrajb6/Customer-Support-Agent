package com.example.email_processing_service.mapper;

import com.example.email_processing_service.dto.DraftResponse;
import com.example.email_processing_service.dto.EmailResponse;
import com.example.email_processing_service.entity.DraftEntity;
import com.example.email_processing_service.entity.EmailEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public EmailResponse toResponse(EmailEntity email) {
        return EmailResponse.builder()
                .id(email.getId())
                .gmailId(email.getGmailId())
                .threadId(email.getThreadId())
                .sender(email.getSender())
                .subject(email.getSubject())
                .body(email.getBody())
                .status(email.getStatus())
                .createdAt(email.getCreatedAt())
                .build();
    }

    public DraftResponse toResponse(DraftEntity draft) {
        return DraftResponse.builder()
                .id(draft.getId())
                .emailId(draft.getEmail().getId())
                .content(draft.getContent())
                .confidence(draft.getConfidence())
                .status(draft.getStatus())
                .createdAt(draft.getCreatedAt())
                .build();
    }
}
