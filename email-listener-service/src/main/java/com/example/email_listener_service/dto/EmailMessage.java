package com.example.email_listener_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Serializable DTO that carries extracted email data through Kafka.
 * MimeMessage cannot be sent directly — it is not serializable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {

    private String messageId;
    private String from;
    private String subject;
    private String body;
    private Instant receivedAt;
}
