package com.theplus.codealchemist.reasoning;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(AlchemistReasoningEngine.class)
@Slf4j
public class FallbackReasoningEngine implements ReasoningEngine {

    @Override
    public String analyze(String systemPrompt, String userPrompt) {
        log.warn("Using fallback reasoning engine — no Anthropic API key configured");
        return """
                {
                  "warning": "No LLM configured. Set ANTHROPIC_API_KEY to enable AI-powered analysis.",
                  "fallback": true
                }
                """;
    }

    @Override
    public int getLastTokenCount() {
        return 0;
    }
}
