package com.theplus.cyberguard.alchemist.controller;

import com.theplus.cyberguard.alchemist.domain.SourceFile;
import com.theplus.cyberguard.alchemist.ingestion.IngestionService;
import com.theplus.cyberguard.alchemist.repository.SourceFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/files")
@RequiredArgsConstructor
public class FileController {

    private final SourceFileRepository sourceFileRepository;
    private final IngestionService ingestionService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable UUID projectId,
            @RequestParam("files") List<MultipartFile> files) {
        int ingested = 0;
        for (MultipartFile file : files) {
            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                ingestionService.ingest(projectId, file.getOriginalFilename(), content);
                ingested++;
            } catch (Exception e) {
                // skip unreadable files
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("filesIngested", ingested, "totalUploaded", files.size()));
    }

    @PostMapping("/inline")
    public ResponseEntity<Map<String, Object>> inlineUpload(
            @PathVariable UUID projectId,
            @RequestBody Map<String, String> body) {
        String filename = body.get("filename");
        String content = body.get("content");
        if (filename == null || content == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "filename and content required"));
        }
        ingestionService.ingest(projectId, filename, content);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("filename", filename, "status", "INGESTED"));
    }

    @GetMapping
    public List<SourceFile> list(@PathVariable UUID projectId) {
        return sourceFileRepository.findByProjectId(projectId);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<SourceFile> get(@PathVariable UUID projectId, @PathVariable UUID fileId) {
        return sourceFileRepository.findById(fileId)
                .filter(f -> f.getProjectId().equals(projectId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable UUID projectId, @PathVariable UUID fileId) {
        return sourceFileRepository.findById(fileId)
                .filter(f -> f.getProjectId().equals(projectId))
                .map(f -> {
                    sourceFileRepository.delete(f);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
