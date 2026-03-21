package com.example.email_listener_service.kafka;

import com.example.email_listener_service.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailKafkaProducer {

    private static final String TOPIC = "incoming-emails";

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;

    public void sendEmail(EmailMessage emailMessage) {
        log.info("📤 Publishing email to Kafka topic '{}': subject='{}'", TOPIC, emailMessage.getSubject());
        kafkaTemplate.send(TOPIC, emailMessage.getMessageId(), emailMessage)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Failed to publish email to Kafka", ex);
                    } else {
                        log.info("✅ Email published to Kafka: partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
