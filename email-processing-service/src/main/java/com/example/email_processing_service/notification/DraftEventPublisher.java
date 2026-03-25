package com.example.email_processing_service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DraftEventPublisher {

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().data(Map.of(
                    "type", "CONNECTED",
                    "timestamp", Instant.now().toString()
            )));
        } catch (Exception e) {
            emitters.remove(emitter);
            log.warn("Failed to send initial SSE event", e);
        }

        return emitter;
    }

    public void publishDraftChanged(Long draftId, Long emailId, String status) {
        publish(Map.of(
                "type", "DRAFT_CHANGED",
                "draftId", draftId,
                "emailId", emailId,
                "status", status,
                "timestamp", Instant.now().toString()
        ));
    }

    private void publish(Object payload) {
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(payload));
                return false;
            } catch (Exception e) {
                log.debug("Removing failed SSE emitter: {}", e.getMessage());
                return true;
            }
        });
    }
}
