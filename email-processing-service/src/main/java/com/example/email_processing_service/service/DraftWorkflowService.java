package com.example.email_processing_service.service;

import com.example.email_processing_service.dto.EditDraftRequest;
import com.example.email_processing_service.entity.DraftEntity;
import com.example.email_processing_service.entity.DraftStatus;
import com.example.email_processing_service.entity.EmailEntity;
import com.example.email_processing_service.entity.EmailStatus;
import com.example.email_processing_service.gmail.GmailSenderService;
import com.example.email_processing_service.notification.DraftEventPublisher;
import com.example.email_processing_service.repository.DraftRepository;
import com.example.email_processing_service.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DraftWorkflowService {

    private final DraftRepository draftRepository;
    private final EmailRepository emailRepository;
    private final GmailSenderService gmailSenderService;
    private final DraftEventPublisher draftEventPublisher;

    @Transactional
    public DraftEntity edit(Long draftId, EditDraftRequest request) {
        DraftEntity draft = getDraftForUpdate(draftId);
        if (draft.getStatus() == DraftStatus.SENT) {
            throw new IllegalStateException("Draft already sent and cannot be edited");
        }
        draft.setContent(request.getContent());
        draft.setStatus(DraftStatus.DRAFT_READY);
        DraftEntity saved = draftRepository.save(draft);
        publishDraftEventSafe(saved);
        return saved;
    }

    @Transactional
    public DraftEntity reject(Long draftId) {
        DraftEntity draft = getDraftForUpdate(draftId);
        if (draft.getStatus() == DraftStatus.SENT) {
            throw new IllegalStateException("Draft already sent and cannot be rejected");
        }
        if (draft.getStatus() == DraftStatus.REJECTED) {
            throw new IllegalStateException("Draft is already rejected");
        }
        draft.setStatus(DraftStatus.REJECTED);
        DraftEntity saved = draftRepository.save(draft);

        EmailEntity email = saved.getEmail();
        email.setStatus(EmailStatus.REJECTED);
        emailRepository.save(email);
        publishDraftEventSafe(saved);
        return saved;
    }

    @Transactional
    public DraftEntity approve(Long draftId) {
        DraftEntity draft = getDraftForUpdate(draftId);
        EmailEntity email = draft.getEmail();
        if (draft.getStatus() == DraftStatus.SENT || email.getStatus() == EmailStatus.SENT) {
            return draft;
        }
        if (draft.getStatus() == DraftStatus.REJECTED || email.getStatus() == EmailStatus.REJECTED) {
            throw new IllegalStateException("Rejected draft cannot be sent.");
        }

        draft.setStatus(DraftStatus.APPROVED);
        email.setStatus(EmailStatus.APPROVED);
        draftRepository.save(draft);
        emailRepository.save(email);

        gmailSenderService.sendReply(email.getThreadId(), email.getSender(), email.getSubject(), draft.getContent());

        draft.setStatus(DraftStatus.SENT);
        email.setStatus(EmailStatus.SENT);
        DraftEntity saved = draftRepository.save(draft);
        emailRepository.save(email);
        publishDraftEventSafe(saved);
        return saved;
    }

    private DraftEntity getDraft(Long draftId) {
        return draftRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found: " + draftId));
    }

    private DraftEntity getDraftForUpdate(Long draftId) {
        return draftRepository.findByIdForUpdate(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found: " + draftId));
    }

    private void publishDraftEventSafe(DraftEntity draft) {
        try {
            draftEventPublisher.publishDraftChanged(draft.getId(), draft.getEmail().getId(), draft.getStatus().name());
        } catch (Exception ignored) {
            // Draft persistence must not fail because SSE client disconnected.
        }
    }
}
