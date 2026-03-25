package com.example.email_listener_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {

    private String gmailId;
    private String threadId;
    private String sender;
    private String subject;
    private String body;
    private Instant receivedAt;
}
