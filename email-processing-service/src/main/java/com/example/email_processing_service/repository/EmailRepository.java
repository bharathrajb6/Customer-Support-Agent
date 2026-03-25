package com.example.email_processing_service.repository;

import com.example.email_processing_service.entity.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailEntity, Long> {
    Optional<EmailEntity> findByGmailId(String gmailId);
}
