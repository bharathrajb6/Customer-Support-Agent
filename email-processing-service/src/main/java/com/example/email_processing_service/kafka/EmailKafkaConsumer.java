package com.example.email_processing_service.kafka;

import com.example.email_processing_service.dto.EmailMessage;
import com.example.email_processing_service.service.EmailProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailKafkaConsumer {

    private final EmailProcessingService emailProcessingService;

    @KafkaListener(topics = "incoming-emails", groupId = "email-processing-group")
    public void consume(EmailMessage emailMessage) {
        log.info("📥 Received email from Kafka: subject='{}'", emailMessage.getSubject());
        emailProcessingService.processEmail(emailMessage);
    }
}
