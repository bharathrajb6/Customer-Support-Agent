package com.example.email_processing_service.controller;

import com.example.email_processing_service.dto.DraftResponse;
import com.example.email_processing_service.dto.EditDraftRequest;
import com.example.email_processing_service.mapper.ApiMapper;
import com.example.email_processing_service.repository.DraftRepository;
import com.example.email_processing_service.service.DraftWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftRepository draftRepository;
    private final DraftWorkflowService draftWorkflowService;
    private final ApiMapper mapper;

    @GetMapping
    public List<DraftResponse> getDrafts() {
        return draftRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/{id}/approve")
    public DraftResponse approve(@PathVariable Long id) {
        return mapper.toResponse(draftWorkflowService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public DraftResponse reject(@PathVariable Long id) {
        return mapper.toResponse(draftWorkflowService.reject(id));
    }

    @PutMapping("/{id}")
    public DraftResponse edit(@PathVariable Long id, @Valid @RequestBody EditDraftRequest request) {
        return mapper.toResponse(draftWorkflowService.edit(id, request));
    }
}
