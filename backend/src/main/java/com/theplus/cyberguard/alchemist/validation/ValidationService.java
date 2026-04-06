package com.theplus.cyberguard.alchemist.validation;

import com.theplus.cyberguard.alchemist.domain.ExtractedRule;
import com.theplus.cyberguard.alchemist.domain.GeneratedArtifact;
import com.theplus.cyberguard.alchemist.domain.SourceFile;
import com.theplus.cyberguard.alchemist.reasoning.ReasoningEngine;
import com.theplus.cyberguard.alchemist.repository.ExtractedRuleRepository;
import com.theplus.cyberguard.alchemist.repository.GeneratedArtifactRepository;
import com.theplus.cyberguard.alchemist.repository.SourceFileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationService {

    private final SourceFileRepository sourceFileRepository;
    private final ExtractedRuleRepository ruleRepository;
    private final GeneratedArtifactRepository artifactRepository;
    private final ReasoningEngine reasoningEngine;
    private final ObjectMapper objectMapper;

    public Map<String, Object> validate(UUID projectId) {
        List<ExtractedRule> rules = ruleRepository.findByProjectIdAndApprovalStatus(projectId, "APPROVED");
        List<SourceFile> sourceFiles = sourceFileRepository.findByProjectId(projectId);
        List<GeneratedArtifact> generatedCode = artifactRepository
                .findByProjectIdAndArtifactType(projectId, "JAVA_SOURCE");

        String systemPrompt = """
                You are a validation expert comparing legacy code behavior with generated modern code.
                For each business rule, verify that the modern code faithfully implements the same logic.

                Output a JSON object:
                {
                  "rules_validated": number,
                  "rules_matching": number,
                  "rules_with_discrepancies": number,
                  "validations": [
                    {
                      "rule_name": "string",
                      "status": "MATCH" | "DISCREPANCY" | "UNABLE_TO_VERIFY",
                      "confidence": number (0-1),
                      "details": "string",
                      "test_scenario": "string"
                    }
                  ],
                  "overall_confidence": number (0-1)
                }
                """;

        String userPrompt = buildValidationPrompt(rules, sourceFiles, generatedCode);
        String response = reasoningEngine.analyze(systemPrompt, userPrompt);
        int tokens = reasoningEngine.getLastTokenCount();

        // Store validation report
        String hash = sha256(response + projectId + Instant.now());
        GeneratedArtifact report = GeneratedArtifact.builder()
                .projectId(projectId)
                .artifactType("VALIDATION_REPORT")
                .filename("validation-report.json")
                .content(response)
                .contentHash(hash)
                .signature(hash)
                .generationModel("claude-sonnet-4-6")
                .generationPromptVersion("v1")
                .build();
        artifactRepository.save(report);

        log.info("Validation complete for project {}: {} rules checked ({} tokens)",
                projectId, rules.size(), tokens);

        return Map.of(
                "rulesValidated", rules.size(),
                "reportArtifactId", report.getId().toString(),
                "tokensUsed", tokens
        );
    }

    private String buildValidationPrompt(List<ExtractedRule> rules, List<SourceFile> sourceFiles,
                                          List<GeneratedArtifact> generatedCode) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Legacy Source Files\n\n");
        for (SourceFile sf : sourceFiles) {
            if (sf.getRawContent() != null) {
                String truncated = sf.getRawContent().length() > 10000
                        ? sf.getRawContent().substring(0, 10000) + "\n...(truncated)"
                        : sf.getRawContent();
                sb.append(String.format("### %s (%s)\n```\n%s\n```\n\n",
                        sf.getFilename(), sf.getLanguage(), truncated));
            }
        }

        sb.append("## Business Rules\n\n");
        for (ExtractedRule rule : rules) {
            sb.append(String.format("- **%s**: %s\n  Pseudo-code: %s\n\n",
                    rule.getRuleName(), rule.getDescription(),
                    rule.getPseudoCode() != null ? rule.getPseudoCode() : "N/A"));
        }

        sb.append("## Generated Modern Code\n\n");
        for (GeneratedArtifact artifact : generatedCode) {
            String truncated = artifact.getContent().length() > 5000
                    ? artifact.getContent().substring(0, 5000) + "\n...(truncated)"
                    : artifact.getContent();
            sb.append(String.format("### %s\n```java\n%s\n```\n\n",
                    artifact.getFilename(), truncated));
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
