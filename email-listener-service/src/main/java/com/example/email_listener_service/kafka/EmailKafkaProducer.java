package com.example.email_listener_service.kafka;

import com.example.email_listener_service.dto.EmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.incoming}")
    private String incomingTopic;

    public void sendEmail(EmailMessage emailMessage) {
        try {
            String payload = objectMapper.writeValueAsString(emailMessage);
            kafkaTemplate.send(incomingTopic, emailMessage.getGmailId(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish email to Kafka", ex);
                            return;
                        }
                        log.info("Email published: gmailId={} partition={} offset={}",
                                emailMessage.getGmailId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    });
        } catch (Exception e) {
            log.error("Failed to serialize email payload", e);
        }
    }
}
