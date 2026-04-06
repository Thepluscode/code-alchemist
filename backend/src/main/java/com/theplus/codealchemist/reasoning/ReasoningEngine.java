package com.theplus.codealchemist.reasoning;

public interface ReasoningEngine {
    String analyze(String systemPrompt, String userPrompt);
    int getLastTokenCount();
}
