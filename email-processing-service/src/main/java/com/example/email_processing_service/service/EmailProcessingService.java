package com.example.email_processing_service.service;

import com.example.email_processing_service.dto.EmailMessage;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailProcessingService {

    /**
     * Processes an email received via Kafka.
     * This is where you can add AI classification, ticket creation, auto-reply
     * logic, etc.
     */
    public void processEmail(EmailMessage email) {
        log.info("📧 Processing Email:");
        log.info("From: {}", email.getFrom());
        log.info("Subject: {}", email.getSubject());
        log.info("Body: {}", email.getBody());
        log.info("MessageID: {}", email.getMessageId());
        log.info("ReceivedAt: {}", email.getReceivedAt());
        log.info("✅ Email processed successfully");

        // TODO: Add your processing logic here
        // e.g., AI classification, sentiment analysis, auto-reply, ticketing, etc.
    }
}
