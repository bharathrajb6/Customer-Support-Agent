package com.example.email_processing_service.gmail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${EMAIL_USERNAME}")
    private String fromAddress;

    public void sendReply(String threadId, String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(buildReplySubject(subject));
            message.setText(body);
            mailSender.send(message);
            log.info("Sent SMTP reply for threadId={} to={}", threadId, to);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send SMTP reply", e);
        }
    }

    private String buildReplySubject(String subject) {
        String normalized = subject == null ? "" : subject.trim();
        if (normalized.toLowerCase().startsWith("re:")) {
            return normalized;
        }
        return "Re: " + normalized;
    }
}
