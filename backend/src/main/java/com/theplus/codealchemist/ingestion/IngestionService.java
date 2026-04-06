package com.theplus.codealchemist.ingestion;

import com.theplus.codealchemist.domain.SourceFile;
import com.theplus.codealchemist.repository.SourceFileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

    private final SourceFileRepository sourceFileRepository;
    private final LanguageDetector languageDetector;
    private final ObjectMapper objectMapper;

    /**
     * Ingest a single file into the project (used by upload endpoints).
     * Creates a new SourceFile, detects language, computes hash, and parses structure.
     */
    public SourceFile ingest(UUID projectId, String filename, String content) {
        String lang = languageDetector.detect(filename, content);
        String hash = sha256(content);

        SourceFile file = SourceFile.builder()
                .projectId(projectId)
                .filename(filename)
                .language(lang)
                .rawContent(content)
                .contentHash(hash)
                .lineCount((int) content.lines().count())
                .build();

        try {
            Map<String, Object> ast = parseStructure(lang, content);
            file.setParsedAst(objectMapper.writeValueAsString(ast));
        } catch (Exception e) {
            log.warn("Failed to parse structure for {}: {}", filename, e.getMessage());
        }

        SourceFile saved = sourceFileRepository.save(file);
        log.info("Ingested {} as {} ({} chars)", filename, lang, content.length());
        return saved;
    }

    /**
     * Re-parse existing source files for a project (called by pipeline ingestion step).
     * Ensures language detection, content hash, and AST are populated.
     */
    public Map<String, Object> ingest(UUID projectId) {
        List<SourceFile> files = sourceFileRepository.findByProjectId(projectId);
        if (files.isEmpty()) {
            throw new IllegalStateException("No source files uploaded for project " + projectId);
        }

        int parsed = 0;
        Map<String, Integer> languageCounts = new HashMap<>();

        for (SourceFile file : files) {
            try {
                String content = file.getRawContent();
                if (content == null || content.isBlank()) continue;

                if (file.getLanguage() == null) {
                    file.setLanguage(languageDetector.detect(file.getFilename(), content));
                }
                if (file.getContentHash() == null) {
                    file.setContentHash(sha256(content));
                }
                if (file.getLineCount() == null) {
                    file.setLineCount((int) content.lines().count());
                }

                Map<String, Object> ast = parseStructure(file.getLanguage(), content);
                file.setParsedAst(objectMapper.writeValueAsString(ast));
                sourceFileRepository.save(file);

                languageCounts.merge(file.getLanguage(), 1, Integer::sum);
                parsed++;

                log.info("Parsed {} as {} ({} chars)", file.getFilename(), file.getLanguage(), content.length());
            } catch (Exception e) {
                log.error("Failed to parse {}: {}", file.getFilename(), e.getMessage());
            }
        }

        return Map.of(
                "totalFiles", files.size(),
                "parsedFiles", parsed,
                "languageCounts", languageCounts
        );
    }

    private Map<String, Object> parseStructure(String language, String content) {
        if (language == null) return Map.of("type", "raw", "lineCount", content.lines().count());
        return switch (language) {
            case "COBOL" -> parseCobolStructure(content);
            case "VB6" -> parseVb6Structure(content);
            case "RPG" -> parseRpgStructure(content);
            default -> Map.of("type", "raw", "lineCount", content.lines().count());
        };
    }

    private Map<String, Object> parseCobolStructure(String content) {
        Map<String, Object> ast = new HashMap<>();
        ast.put("type", "COBOL");

        List<String> divisions = new ArrayList<>();
        Pattern divPattern = Pattern.compile("(?m)^.{6} (\\w[\\w-]+ DIVISION)", Pattern.CASE_INSENSITIVE);
        Matcher m = divPattern.matcher(content);
        while (m.find()) divisions.add(m.group(1).toUpperCase());
        ast.put("divisions", divisions);

        List<String> paragraphs = new ArrayList<>();
        Pattern paraPattern = Pattern.compile("(?m)^.{6} (\\w[\\w-]+)\\.$");
        Matcher pm = paraPattern.matcher(content);
        while (pm.find()) paragraphs.add(pm.group(1));
        ast.put("paragraphs", paragraphs);

        List<String> copies = new ArrayList<>();
        Pattern copyPattern = Pattern.compile("(?i)COPY\\s+(\\S+)", Pattern.MULTILINE);
        Matcher cm = copyPattern.matcher(content);
        while (cm.find()) copies.add(cm.group(1).replace(".", ""));
        ast.put("copybooks", copies);

        List<String> calls = new ArrayList<>();
        Pattern callPattern = Pattern.compile("(?i)CALL\\s+['\"]?(\\w+)['\"]?", Pattern.MULTILINE);
        Matcher callM = callPattern.matcher(content);
        while (callM.find()) calls.add(callM.group(1));
        ast.put("externalCalls", calls);

        List<String> fileOps = new ArrayList<>();
        Pattern selectPattern = Pattern.compile("(?i)SELECT\\s+(\\S+)\\s+ASSIGN", Pattern.MULTILINE);
        Matcher sm = selectPattern.matcher(content);
        while (sm.find()) fileOps.add(sm.group(1));
        ast.put("fileOperations", fileOps);

        ast.put("lineCount", content.lines().count());
        return ast;
    }

    private Map<String, Object> parseVb6Structure(String content) {
        Map<String, Object> ast = new HashMap<>();
        ast.put("type", "VB6");

        List<String> subs = new ArrayList<>();
        Pattern subPattern = Pattern.compile("(?i)(Public|Private)?\\s*(Sub|Function)\\s+(\\w+)");
        Matcher m = subPattern.matcher(content);
        while (m.find()) subs.add(m.group(3));
        ast.put("procedures", subs);

        List<String> createObjects = new ArrayList<>();
        Pattern coPattern = Pattern.compile("(?i)CreateObject\\(['\"]([^'\"]+)['\"]\\)");
        Matcher cm = coPattern.matcher(content);
        while (cm.find()) createObjects.add(cm.group(1));
        ast.put("externalObjects", createObjects);

        ast.put("lineCount", content.lines().count());
        return ast;
    }

    private Map<String, Object> parseRpgStructure(String content) {
        Map<String, Object> ast = new HashMap<>();
        ast.put("type", "RPG");

        List<String> subroutines = new ArrayList<>();
        Pattern srPattern = Pattern.compile("(?i)BEGSR\\s+(\\w+)");
        Matcher m = srPattern.matcher(content);
        while (m.find()) subroutines.add(m.group(1));
        ast.put("subroutines", subroutines);

        ast.put("lineCount", content.lines().count());
        return ast;
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return "hash-error";
        }
    }
}
