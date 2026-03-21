package com.example.email_listener_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mail.dsl.Mail;
import com.example.email_listener_service.service.EmailProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ImapIntegrationConfig {

    private final EmailProcessingService emailProcessingService;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Bean
    public IntegrationFlow imapIdleFlow() {
        // App passwords often contain spaces which break URI parsing.
        String cleanPassword = password != null ? password.replace(" ", "") : "";

        // URL encode the username to handle special characters such as '@' properly
        String encodedUsername = username;
        String encodedPassword = cleanPassword;
        try {
            encodedUsername = URLEncoder.encode(username, "UTF-8");
            encodedPassword = URLEncoder.encode(cleanPassword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to URL encode IMAP credentials", e);
        }

        String imapUrl = String.format("imaps://%s:%s@%s:%d/INBOX", encodedUsername, encodedPassword, host, port);

        log.info("Starting IMAP IDLE listener on {}:{}", host, port);

        return IntegrationFlow.from(Mail.imapIdleAdapter(imapUrl)
                .shouldDeleteMessages(false)
                .shouldMarkMessagesAsRead(true)
                .simpleContent(true)
                .javaMailProperties(p -> p
                        .put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                        .put("mail.imap.socketFactory.fallback", "false")
                        .put("mail.store.protocol", "imaps")
                        .put("mail.debug", "false")))
                .handle(message -> {
                    MimeMessage mimeMessage = (MimeMessage) message.getPayload();
                    log.info("New email notification received via IMAP IDLE flow.");
                    emailProcessingService.processEmail(mimeMessage);
                })
                .get();
    }
}
