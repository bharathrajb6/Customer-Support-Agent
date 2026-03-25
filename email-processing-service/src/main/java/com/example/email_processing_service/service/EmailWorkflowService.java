package com.example.email_processing_service.service;

import com.example.email_processing_service.ai.agent.CustomerSupportAgentService;
import com.example.email_processing_service.ai.agent.DraftGenerationResult;
import com.example.email_processing_service.dto.EmailMessage;
import com.example.email_processing_service.entity.DraftEntity;
import com.example.email_processing_service.entity.DraftStatus;
import com.example.email_processing_service.entity.EmailEntity;
import com.example.email_processing_service.entity.EmailStatus;
import com.example.email_processing_service.notification.DraftEventPublisher;
import com.example.email_processing_service.repository.DraftRepository;
import com.example.email_processing_service.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailWorkflowService {

    private final EmailRepository emailRepository;
    private final DraftRepository draftRepository;
    private final CustomerSupportAgentService agentService;
    private final DraftEventPublisher draftEventPublisher;

    @Transactional
    public void processIncomingEmail(EmailMessage event) {
        Optional<EmailEntity> existing = emailRepository.findByGmailId(event.getGmailId());
        if (existing.isPresent()) {
            log.info("Skipping already processed gmailId={}", event.getGmailId());
            return;
        }

        EmailEntity email = new EmailEntity();
        email.setGmailId(event.getGmailId());
        email.setThreadId(event.getThreadId());
        email.setSender(event.getSender());
        email.setSubject(event.getSubject());
        email.setBody(event.getBody());
        email.setStatus(EmailStatus.NEW);
        email = emailRepository.save(email);

        DraftEntity draft = new DraftEntity();
        draft.setEmail(email);
        String generationSource = "AI_GENERATED";
        try {
            DraftGenerationResult generated = CompletableFuture
                    .supplyAsync(() -> agentService.processEmail(event))
                    .orTimeout(20, TimeUnit.SECONDS)
                    .join();
            draft.setContent(generated.getDraft());
            draft.setConfidence(generated.getConfidence() != null ? generated.getConfidence() : 0.6);
        } catch (Exception ex) {
            generationSource = "FALLBACK_GENERATED";
            log.warn("AI draft generation failed for gmailId={}, creating fallback draft", event.getGmailId(), ex);
            draft.setContent(buildFallbackDraft(event));
            draft.setConfidence(0.2);
        }
        draft.setStatus(DraftStatus.DRAFT_READY);
        draft = draftRepository.save(draft);

        email.setStatus(EmailStatus.DRAFT_READY);
        emailRepository.save(email);
        log.info("Draft creation finished for gmailId={} source={} draftId={} confidence={}",
                event.getGmailId(), generationSource, draft.getId(), draft.getConfidence());
        try {
            draftEventPublisher.publishDraftChanged(draft.getId(), email.getId(), draft.getStatus().name());
        } catch (Exception ex) {
            log.warn("SSE publish failed after draft creation for draftId={}", draft.getId(), ex);
        }
    }

    private String buildFallbackDraft(EmailMessage event) {
        return "Hi,\n\n"
                + "Thanks for reaching out. We received your email"
                + (event.getSubject() != null && !event.getSubject().isBlank() ? " regarding \"" + event.getSubject() + "\"" : "")
                + ".\n"
                + "Our support team is reviewing it and will get back to you shortly.\n\n"
                + "Regards,\nSupport Team";
    }
}
