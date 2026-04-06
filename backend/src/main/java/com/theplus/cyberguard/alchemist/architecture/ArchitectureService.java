package com.theplus.cyberguard.alchemist.architecture;

import com.theplus.cyberguard.alchemist.domain.ExtractedRule;
import com.theplus.cyberguard.alchemist.domain.GeneratedArtifact;
import com.theplus.cyberguard.alchemist.graph.GraphQueryService;
import com.theplus.cyberguard.alchemist.reasoning.ReasoningEngine;
import com.theplus.cyberguard.alchemist.repository.ExtractedRuleRepository;
import com.theplus.cyberguard.alchemist.repository.GeneratedArtifactRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArchitectureService {

    private final ExtractedRuleRepository extractedRuleRepository;
    private final GeneratedArtifactRepository artifactRepository;
    private final GraphQueryService graphQueryService;
    private final ReasoningEngine reasoningEngine;
    private final ObjectMapper objectMapper;

    private String systemPrompt;

    public Map<String, Object> design(UUID projectId) {
        loadSystemPrompt();

        // Gather approved rules
        List<ExtractedRule> rules = extractedRuleRepository.findByProjectIdAndApprovalStatus(projectId, "APPROVED");
        if (rules.isEmpty()) {
            throw new IllegalStateException("No approved rules found for architecture design");
        }

        // Get graph metrics for context
        Map<String, Object> graphMetrics = graphQueryService.getMetrics(projectId);

        String userPrompt = buildUserPrompt(rules, graphMetrics);
        String response = reasoningEngine.analyze(systemPrompt, userPrompt);
        int tokens = reasoningEngine.getLastTokenCount();

        // Store architecture design as artifact
        String contentHash = sha256(response + projectId + Instant.now());
        GeneratedArtifact artifact = GeneratedArtifact.builder()
                .projectId(projectId)
                .artifactType("ARCHITECTURE_DESIGN")
                .filename("architecture-design.json")
                .content(response)
                .contentHash(contentHash)
                .signature(contentHash)
                .generationModel("claude-sonnet-4-6")
                .generationPromptVersion("v1")
                .build();
        artifactRepository.save(artifact);

        log.info("Generated architecture design for project {} ({} tokens)", projectId, tokens);

        return Map.of(
                "artifactId", artifact.getId().toString(),
                "tokensUsed", tokens,
                "approvedRuleCount", rules.size()
        );
    }

    private void loadSystemPrompt() {
        if (systemPrompt != null) return;
        try {
            var resource = new ClassPathResource("prompts/architecture-v1.txt");
            systemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            systemPrompt = "Design a cloud-native microservice architecture for the given legacy system.";
        }
    }

    private String buildUserPrompt(List<ExtractedRule> rules, Map<String, Object> graphMetrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Approved Business Rules\n\n");
        for (ExtractedRule rule : rules) {
            sb.append(String.format("- **%s** (%s): %s\n  Pseudo-code: %s\n\n",
                    rule.getRuleName(), rule.getRuleType(), rule.getDescription(),
                    rule.getPseudoCode() != null ? rule.getPseudoCode() : "N/A"));
        }
        sb.append("\n## Dependency Graph Metrics\n\n");
        try {
            sb.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(graphMetrics));
        } catch (Exception e) {
            sb.append("(metrics unavailable)");
        }
        return sb.toString();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
