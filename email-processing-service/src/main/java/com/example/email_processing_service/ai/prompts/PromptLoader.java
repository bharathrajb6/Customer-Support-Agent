package com.example.email_processing_service.ai.prompts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PromptLoader {

    public String load(String path) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Prompt file missing, using fallback prompt: {}", path);
            return fallbackPrompt(path);
        }
    }

    private String fallbackPrompt(String path) {
        return switch (path) {
            case "ai/prompts/system_prompt.txt" ->
                    "You are a customer-support AI assistant. Output valid JSON only.";
            case "ai/prompts/intent_prompt.txt" ->
                    "Return JSON: {\"intent\":\"GENERAL\",\"entities\":{},\"tools\":[\"knowledge_base_search\"],\"confidence\":0.5}";
            case "ai/prompts/draft_prompt.txt" ->
                    "Return JSON: {\"draft\":\"Hi, thanks for reaching out. We are reviewing your request and will follow up shortly.\",\"confidence\":0.5}";
            case "ai/prompts/guardrail_prompt.txt" ->
                    "Return JSON: {\"safe\":true,\"reason\":\"fallback\"}";
            default ->
                    "Return valid JSON.";
        };
    }
}
