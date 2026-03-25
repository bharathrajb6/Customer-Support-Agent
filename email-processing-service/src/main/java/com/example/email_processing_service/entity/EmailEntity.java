package com.example.email_processing_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "emails", uniqueConstraints = @UniqueConstraint(name = "uk_email_gmail_id", columnNames = "gmail_id"))
@Getter
@Setter
public class EmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gmail_id", nullable = false, length = 255)
    private String gmailId;

    @Column(name = "thread_id", nullable = false, length = 255)
    private String threadId;

    @Column(nullable = false, length = 512)
    private String sender;

    @Column(nullable = false, length = 512)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EmailStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
