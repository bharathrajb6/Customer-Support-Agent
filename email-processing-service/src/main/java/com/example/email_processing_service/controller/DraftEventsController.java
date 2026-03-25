package com.example.email_processing_service.controller;

import com.example.email_processing_service.notification.DraftEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class DraftEventsController {

    private final DraftEventPublisher draftEventPublisher;

    @GetMapping(value = "/drafts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDraftEvents() {
        return draftEventPublisher.subscribe();
    }
}
