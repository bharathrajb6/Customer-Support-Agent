package com.example.email_processing_service.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class LlmConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${app.llm.api-key}") String apiKey,
            @Value("${app.llm.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${app.llm.model:gpt-4o-mini}") String model,
            @Value("${app.llm.temperature:0.2}") Double temperature
    ) {
        String maskedApiKey = maskApiKey(apiKey);
        log.info("Initializing LLM client: model={}, baseUrl={}, temperature={}, apiKey={}",
                model, baseUrl, temperature, maskedApiKey);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(45))
                .build();
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "missing";
        }
        if (apiKey.length() <= 8) {
            return "present(len=" + apiKey.length() + ")";
        }
        String prefix = apiKey.substring(0, 4);
        String suffix = apiKey.substring(apiKey.length() - 4);
        return prefix + "..." + suffix + "(len=" + apiKey.length() + ")";
    }
}
