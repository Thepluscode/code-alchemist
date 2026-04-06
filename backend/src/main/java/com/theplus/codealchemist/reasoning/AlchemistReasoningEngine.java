package com.theplus.codealchemist.reasoning;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnBean(AnthropicClient.class)
public class AlchemistReasoningEngine implements ReasoningEngine {

    private final AnthropicClient client;
    private final String model;
    private final int maxTokens;
    private int lastTokenCount;

    public AlchemistReasoningEngine(
            AnthropicClient alchemistAnthropicClient,
            @Value("${alchemist.reasoning.model:claude-sonnet-4-6}") String model,
            @Value("${alchemist.reasoning.max-tokens:8192}") int maxTokens) {
        this.client = alchemistAnthropicClient;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    @Override
    public String analyze(String systemPrompt, String userPrompt) {
        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(model)
                    .maxTokens(maxTokens)
                    .system(systemPrompt)
                    .addUserMessage(userPrompt)
                    .build();

            Message response = client.messages().create(params);

            lastTokenCount = (int) (response.usage().inputTokens() + response.usage().outputTokens());

            StringBuilder content = new StringBuilder();
            for (ContentBlock block : response.content()) {
                block.text().ifPresent(tb -> content.append(tb.text()));
            }

            log.info("Claude analysis completed: {} tokens (model: {})", lastTokenCount, model);
            return content.toString();
        } catch (Exception e) {
            log.error("Claude analysis failed: {}", e.getMessage(), e);
            throw new RuntimeException("LLM analysis failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int getLastTokenCount() {
        return lastTokenCount;
    }
}
