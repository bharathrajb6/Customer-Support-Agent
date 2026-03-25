package com.example.email_processing_service.controller;

import com.example.email_processing_service.dto.EmailResponse;
import com.example.email_processing_service.mapper.ApiMapper;
import com.example.email_processing_service.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailRepository emailRepository;
    private final ApiMapper mapper;

    @GetMapping
    public List<EmailResponse> getEmails() {
        return emailRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailResponse> getEmail(@PathVariable Long id) {
        return emailRepository.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
