package com.theplus.codealchemist.extraction;

import com.theplus.codealchemist.domain.ExtractedRule;
import com.theplus.codealchemist.domain.SourceFile;
import com.theplus.codealchemist.reasoning.ReasoningEngine;
import com.theplus.codealchemist.repository.ExtractedRuleRepository;
import com.theplus.codealchemist.repository.SourceFileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExtractionService {

    private final SourceFileRepository sourceFileRepository;
    private final ExtractedRuleRepository extractedRuleRepository;
    private final ReasoningEngine reasoningEngine;
    private final ObjectMapper objectMapper;

    private String systemPrompt;

    public Map<String, Object> extract(UUID projectId) {
        List<SourceFile> files = sourceFileRepository.findByProjectId(projectId);

        if (files.isEmpty()) {
            throw new IllegalStateException("No source files for extraction in project " + projectId);
        }

        loadSystemPrompt();

        int totalRules = 0;
        int totalTokens = 0;

        for (SourceFile file : files) {
            try {
                String userPrompt = buildUserPrompt(file);
                String response = reasoningEngine.analyze(systemPrompt, userPrompt);
                totalTokens += reasoningEngine.getLastTokenCount();

                List<Map<String, Object>> rules = parseRulesResponse(response);

                for (Map<String, Object> rule : rules) {
                    ExtractedRule entity = ExtractedRule.builder()
                            .projectId(projectId)
                            .sourceFileId(file.getId())
                            .ruleName(getString(rule, "rule_name"))
                            .ruleType(getString(rule, "rule_type"))
                            .description(getString(rule, "description"))
                            .pseudoCode(getString(rule, "pseudo_code"))
                            .confidence(getDouble(rule, "confidence"))
                            .approvalStatus("PENDING")
                            .build();
                    extractedRuleRepository.save(entity);
                    totalRules++;
                }

                log.info("Extracted {} rules from {}", rules.size(), file.getFilename());

            } catch (Exception e) {
                log.error("Extraction failed for {}: {}", file.getFilename(), e.getMessage());
            }
        }

        return Map.of(
                "filesProcessed", files.size(),
                "rulesExtracted", totalRules,
                "tokensUsed", totalTokens
        );
    }

    private void loadSystemPrompt() {
        if (systemPrompt != null) return;
        try {
            var resource = new ClassPathResource("prompts/extraction-v1.txt");
            systemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to load extraction prompt, using fallback");
            systemPrompt = "Extract all business rules from the provided legacy source code as a JSON array.";
        }
    }

    private String buildUserPrompt(SourceFile file) {
        return String.format("""
                Source file: %s
                Language: %s
                Structure: %s

                Source code:
                ```
                %s
                ```

                Extract all business rules as a JSON array.""",
                file.getFilename(),
                file.getLanguage(),
                file.getParsedAst() != null ? file.getParsedAst() : "{}",
                truncateContent(file.getRawContent(), 50000));
    }

    private List<Map<String, Object>> parseRulesResponse(String response) {
        try {
            // Find JSON array in response
            int start = response.indexOf('[');
            int end = response.lastIndexOf(']');
            if (start >= 0 && end > start) {
                String jsonArray = response.substring(start, end + 1);
                return objectMapper.readValue(jsonArray, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse extraction response as JSON: {}", e.getMessage());
        }
        return List.of();
    }

    private String truncateContent(String content, int maxChars) {
        if (content == null) return "";
        return content.length() <= maxChars ? content : content.substring(0, maxChars) + "\n... (truncated)";
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof Number n ? n.doubleValue() : 0.0;
    }
}
