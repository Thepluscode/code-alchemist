package com.theplus.codealchemist.migration;

import com.theplus.codealchemist.domain.ExtractedRule;
import com.theplus.codealchemist.domain.GeneratedArtifact;
import com.theplus.codealchemist.domain.SourceFile;
import com.theplus.codealchemist.reasoning.ReasoningEngine;
import com.theplus.codealchemist.repository.ExtractedRuleRepository;
import com.theplus.codealchemist.repository.GeneratedArtifactRepository;
import com.theplus.codealchemist.repository.SourceFileRepository;
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
public class MigrationPlanService {

    private final SourceFileRepository sourceFileRepository;
    private final ExtractedRuleRepository ruleRepository;
    private final GeneratedArtifactRepository artifactRepository;
    private final ReasoningEngine reasoningEngine;

    private String systemPrompt;

    public Map<String, Object> plan(UUID projectId) {
        loadSystemPrompt();

        List<SourceFile> sourceFiles = sourceFileRepository.findByProjectId(projectId);
        List<ExtractedRule> rules = ruleRepository.findByProjectIdAndApprovalStatus(projectId, "APPROVED");

        Optional<GeneratedArtifact> archArtifact = artifactRepository
                .findByProjectIdAndArtifactType(projectId, "ARCHITECTURE_DESIGN").stream().findFirst();
        Optional<GeneratedArtifact> validationReport = artifactRepository
                .findByProjectIdAndArtifactType(projectId, "VALIDATION_REPORT").stream().findFirst();
        List<GeneratedArtifact> generatedCode = artifactRepository
                .findByProjectIdAndArtifactType(projectId, "JAVA_SOURCE");

        String userPrompt = buildUserPrompt(sourceFiles, rules, archArtifact.orElse(null),
                validationReport.orElse(null), generatedCode);
        String response = reasoningEngine.analyze(systemPrompt, userPrompt);
        int tokens = reasoningEngine.getLastTokenCount();

        String hash = sha256(response + projectId + Instant.now());
        GeneratedArtifact plan = GeneratedArtifact.builder()
                .projectId(projectId)
                .artifactType("MIGRATION_PLAN")
                .filename("migration-plan.json")
                .content(response)
                .contentHash(hash)
                .signature(hash)
                .generationModel("claude-sonnet-4-6")
                .generationPromptVersion("v1")
                .build();
        artifactRepository.save(plan);

        log.info("Generated migration plan for project {} ({} tokens)", projectId, tokens);

        return Map.of(
                "planArtifactId", plan.getId().toString(),
                "tokensUsed", tokens,
                "sourceFileCount", sourceFiles.size(),
                "ruleCount", rules.size()
        );
    }

    private void loadSystemPrompt() {
        if (systemPrompt != null) return;
        try {
            var resource = new ClassPathResource("prompts/migration-v1.txt");
            systemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            systemPrompt = "Create a detailed migration plan for modernizing the given legacy system.";
        }
    }

    private String buildUserPrompt(List<SourceFile> sourceFiles, List<ExtractedRule> rules,
                                    GeneratedArtifact architecture, GeneratedArtifact validation,
                                    List<GeneratedArtifact> generatedCode) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Legacy System Summary\n\n");
        sb.append(String.format("- **Source files**: %d\n", sourceFiles.size()));
        Map<String, Long> langCounts = new HashMap<>();
        for (SourceFile sf : sourceFiles) {
            langCounts.merge(sf.getLanguage(), 1L, Long::sum);
        }
        for (Map.Entry<String, Long> entry : langCounts.entrySet()) {
            sb.append(String.format("  - %s: %d files\n", entry.getKey(), entry.getValue()));
        }
        sb.append(String.format("- **Business rules**: %d approved\n\n", rules.size()));

        sb.append("## Business Rules\n\n");
        for (ExtractedRule rule : rules) {
            sb.append(String.format("- **%s** (%s): %s\n", rule.getRuleName(), rule.getRuleType(), rule.getDescription()));
        }

        if (architecture != null) {
            String arch = architecture.getContent().length() > 8000
                    ? architecture.getContent().substring(0, 8000) + "\n...(truncated)"
                    : architecture.getContent();
            sb.append("\n## Target Architecture\n\n```json\n").append(arch).append("\n```\n\n");
        }

        sb.append(String.format("## Generated Code Artifacts: %d files\n\n", generatedCode.size()));
        for (GeneratedArtifact artifact : generatedCode) {
            sb.append(String.format("- %s (%s)\n", artifact.getFilename(), artifact.getArtifactType()));
        }

        if (validation != null) {
            String val = validation.getContent().length() > 5000
                    ? validation.getContent().substring(0, 5000) + "\n...(truncated)"
                    : validation.getContent();
            sb.append("\n## Validation Report\n\n```json\n").append(val).append("\n```\n\n");
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
