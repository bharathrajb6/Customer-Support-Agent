package com.example.email_processing_service.kafka;

import com.example.email_processing_service.dto.EmailMessage;
import com.example.email_processing_service.service.EmailWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailKafkaConsumer {

    private final EmailWorkflowService emailWorkflowService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic.incoming}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload) {
        try {
            EmailMessage emailMessage = objectMapper.readValue(payload, EmailMessage.class);
            log.info("Received email event: gmailId={} subject={}", emailMessage.getGmailId(), emailMessage.getSubject());
            emailWorkflowService.processIncomingEmail(emailMessage);
        } catch (Exception e) {
            log.error("Failed to process Kafka payload", e);
        }
    }
}
