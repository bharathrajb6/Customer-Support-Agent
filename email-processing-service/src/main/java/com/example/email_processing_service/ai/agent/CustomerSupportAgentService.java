package com.example.email_processing_service.ai.agent;

import com.example.email_processing_service.ai.prompts.PromptLoader;
import com.example.email_processing_service.ai.tools.ToolExecutionService;
import com.example.email_processing_service.dto.EmailMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerSupportAgentService {

    private final ChatLanguageModel chatLanguageModel;
    private final PromptLoader promptLoader;
    private final ToolExecutionService tools;
    private final ObjectMapper objectMapper;

    @Retry(name = "llm")
    public DraftGenerationResult processEmail(EmailMessage email) {
        log.info("AI pipeline start for gmailId={}", email.getGmailId());
        IntentAnalysisResult intent = analyzeIntent(email);
        Map<String, Object> toolOutput = executeTools(intent, email);
        validateGuardrails(email, intent, toolOutput);
        DraftGenerationResult result = generateDraft(email, intent, toolOutput);
        log.info("AI pipeline success for gmailId={} intent={} tools={} draftConfidence={}",
                email.getGmailId(),
                intent.getIntent(),
                intent.getTools(),
                result.getConfidence());
        return result;
    }

    private IntentAnalysisResult analyzeIntent(EmailMessage email) {
        String prompt = promptLoader.load("ai/prompts/system_prompt.txt") + "\n" +
                promptLoader.load("ai/prompts/intent_prompt.txt") + "\n" +
                "EMAIL_JSON:\n" + toJson(email);

        String output = chatLanguageModel.generate(prompt);
        IntentAnalysisResult result = fromJson(output, IntentAnalysisResult.class);
        log.info("Intent analysis complete for gmailId={} intent={} confidence={} tools={}",
                email.getGmailId(),
                result.getIntent(),
                result.getConfidence(),
                result.getTools());
        return result;
    }

    private Map<String, Object> executeTools(IntentAnalysisResult intent, EmailMessage email) {
        Map<String, Object> result = new HashMap<>();
        List<String> toolNames = intent.getTools();
        if (toolNames == null) {
            return result;
        }
        Map<String, String> entities = intent.getEntities() == null ? Map.of() : intent.getEntities();

        for (String tool : toolNames) {
            switch (tool) {
                case "order_service" -> result.put("order_service", tools.orderService(entities.getOrDefault("order_id", "UNKNOWN")));
                case "user_service" -> result.put("user_service", tools.userService(entities.getOrDefault("email", email.getSender())));
                case "knowledge_base_search" -> result.put("knowledge_base_search", tools.knowledgeBaseSearch(entities.getOrDefault("query", email.getSubject())));
                default -> log.warn("Unknown tool requested: {}", tool);
            }
        }

        return result;
    }

    private void validateGuardrails(EmailMessage email, IntentAnalysisResult intent, Map<String, Object> toolOutput) {
        String prompt = promptLoader.load("ai/prompts/guardrail_prompt.txt") + "\n" +
                "EMAIL_JSON:\n" + toJson(email) + "\n" +
                "INTENT_JSON:\n" + toJson(intent) + "\n" +
                "TOOL_OUTPUT_JSON:\n" + toJson(toolOutput);
        String output = chatLanguageModel.generate(prompt);
        Map<String, Object> guard = fromJson(output, new TypeReference<>() {});
        Boolean safe = (Boolean) guard.getOrDefault("safe", Boolean.TRUE);
        log.info("Guardrail result for gmailId={} safe={} reason={}",
                email.getGmailId(),
                safe,
                guard.getOrDefault("reason", "n/a"));
        if (!safe) {
            throw new IllegalStateException("Guardrail blocked response generation");
        }
    }

    private DraftGenerationResult generateDraft(EmailMessage email, IntentAnalysisResult intent, Map<String, Object> toolOutput) {
        String prompt = promptLoader.load("ai/prompts/draft_prompt.txt") + "\n" +
                "EMAIL_JSON:\n" + toJson(email) + "\n" +
                "INTENT_JSON:\n" + toJson(intent) + "\n" +
                "TOOL_OUTPUT_JSON:\n" + toJson(toolOutput);

        String output = chatLanguageModel.generate(prompt);
        DraftGenerationResult result = fromJson(output, DraftGenerationResult.class);
        String preview = result.getDraft() == null ? "" : result.getDraft();
        if (preview.length() > 120) {
            preview = preview.substring(0, 120) + "...";
        }
        log.info("Draft generation complete for gmailId={} confidence={} preview='{}'",
                email.getGmailId(),
                result.getConfidence(),
                preview);
        return result;
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("JSON serialization failed", e);
        }
    }

    private <T> T fromJson(String payload, Class<T> targetType) {
        try {
            return objectMapper.readValue(cleanJson(payload), targetType);
        } catch (Exception e) {
            log.error("Model output parse failed: {}", payload, e);
            throw new IllegalStateException("Model output parse failure", e);
        }
    }

    private <T> T fromJson(String payload, TypeReference<T> targetType) {
        try {
            return objectMapper.readValue(cleanJson(payload), targetType);
        } catch (Exception e) {
            throw new IllegalStateException("Model output parse failure", e);
        }
    }

    private String cleanJson(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            return trimmed.replace("```json", "").replace("```", "").trim();
        }
        return trimmed;
    }
}
