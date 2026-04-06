package com.theplus.codealchemist.codegen;

import com.theplus.codealchemist.domain.ExtractedRule;
import com.theplus.codealchemist.domain.GeneratedArtifact;
import com.theplus.codealchemist.reasoning.ReasoningEngine;
import com.theplus.codealchemist.repository.ExtractedRuleRepository;
import com.theplus.codealchemist.repository.GeneratedArtifactRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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
public class CodeGenerationService {

    private final GeneratedArtifactRepository artifactRepository;
    private final ExtractedRuleRepository ruleRepository;
    private final ReasoningEngine reasoningEngine;
    private final ObjectMapper objectMapper;

    private String systemPrompt;

    public Map<String, Object> generate(UUID projectId) {
        loadSystemPrompt();

        // Get architecture design
        GeneratedArtifact archArtifact = artifactRepository
                .findByProjectIdAndArtifactType(projectId, "ARCHITECTURE_DESIGN").stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No architecture design found"));

        // Get approved rules
        List<ExtractedRule> rules = ruleRepository.findByProjectIdAndApprovalStatus(projectId, "APPROVED");

        String userPrompt = buildUserPrompt(archArtifact.getContent(), rules);
        String response = reasoningEngine.analyze(systemPrompt, userPrompt);
        int tokens = reasoningEngine.getLastTokenCount();

        // Parse generated files from response
        List<Map<String, Object>> files = parseFilesResponse(response);
        int artifactCount = 0;

        for (Map<String, Object> file : files) {
            String content = getString(file, "content");
            String hash = sha256(content + projectId + Instant.now());

            GeneratedArtifact artifact = GeneratedArtifact.builder()
                    .projectId(projectId)
                    .artifactType(getString(file, "artifact_type"))
                    .filename(getString(file, "filename"))
                    .content(content)
                    .contentHash(hash)
                    .signature(hash)
                    .generationModel("claude-sonnet-4-6")
                    .generationPromptVersion("v1")
                    .build();
            artifactRepository.save(artifact);
            artifactCount++;
        }

        log.info("Generated {} artifacts for project {} ({} tokens)", artifactCount, projectId, tokens);

        return Map.of(
                "artifactsGenerated", artifactCount,
                "tokensUsed", tokens
        );
    }

    private void loadSystemPrompt() {
        if (systemPrompt != null) return;
        try {
            var resource = new ClassPathResource("prompts/codegen-v1.txt");
            systemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            systemPrompt = "Generate modern Spring Boot Java code for the given architecture and business rules.";
        }
    }

    private String buildUserPrompt(String architectureJson, List<ExtractedRule> rules) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Architecture Design\n\n```json\n").append(architectureJson).append("\n```\n\n");
        sb.append("## Business Rules to Implement\n\n");
        for (ExtractedRule rule : rules) {
            sb.append(String.format("### %s (%s)\n%s\n```\n%s\n```\n\n",
                    rule.getRuleName(), rule.getRuleType(),
                    rule.getDescription(),
                    rule.getPseudoCode() != null ? rule.getPseudoCode() : ""));
        }
        return sb.toString();
    }

    private List<Map<String, Object>> parseFilesResponse(String response) {
        try {
            int start = response.indexOf('[');
            int end = response.lastIndexOf(']');
            if (start >= 0 && end > start) {
                return objectMapper.readValue(response.substring(start, end + 1), new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse code generation response: {}", e.getMessage());
        }
        return List.of();
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
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
