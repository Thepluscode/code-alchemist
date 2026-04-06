package com.theplus.cyberguard.alchemist.reasoning;

public interface ReasoningEngine {
    String analyze(String systemPrompt, String userPrompt);
    int getLastTokenCount();
}
