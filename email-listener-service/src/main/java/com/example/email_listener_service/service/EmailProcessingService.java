package com.example.email_listener_service.service;

import com.example.email_listener_service.dto.EmailMessage;
import com.example.email_listener_service.kafka.EmailKafkaProducer;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Address;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProcessingService {

    private final EmailKafkaProducer emailKafkaProducer;

    /**
     * Extracts relevant data from the MimeMessage, builds a serializable DTO,
     * and publishes it to Kafka for the processing service to consume.
     */
    public void processEmail(MimeMessage message) {
        try {
            Address[] froms = message.getFrom();
            String sender = froms == null ? "Unknown" : froms[0].toString();
            String subject = message.getSubject();
            String body = message.getContent() != null ? message.getContent().toString() : "";
            String messageId = message.getMessageID();

            log.info("📧 Received New Email:");
            log.info("From: {}", sender);
            log.info("Subject: {}", subject);

            // Build the serializable DTO
            EmailMessage emailMessage = EmailMessage.builder()
                    .messageId(messageId)
                    .from(sender)
                    .subject(subject)
                    .body(body)
                    .receivedAt(Instant.now())
                    .build();

            // Publish to Kafka
            emailKafkaProducer.sendEmail(emailMessage);

        } catch (Exception e) {
            log.error("❌ Error processing incoming email", e);
        }
    }
}
