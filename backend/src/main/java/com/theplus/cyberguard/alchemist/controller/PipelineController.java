package com.theplus.cyberguard.alchemist.controller;

import com.theplus.cyberguard.alchemist.domain.PipelineRun;
import com.theplus.cyberguard.alchemist.pipeline.ModernizationPipeline;
import com.theplus.cyberguard.alchemist.repository.PipelineRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/pipeline")
@RequiredArgsConstructor
public class PipelineController {

    private final ModernizationPipeline pipeline;
    private final PipelineRunRepository pipelineRunRepository;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start(@PathVariable UUID projectId) {
        try {
            pipeline.startPipeline(projectId);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("projectId", projectId.toString(), "status", "STARTED"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resume")
    public ResponseEntity<Map<String, Object>> resume(@PathVariable UUID projectId) {
        try {
            pipeline.resumePipeline(projectId);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("projectId", projectId.toString(), "status", "RESUMED"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/runs")
    public List<PipelineRun> listRuns(@PathVariable UUID projectId) {
        return pipelineRunRepository.findByProjectIdOrderByStartedAtDesc(projectId);
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<PipelineRun> getRun(@PathVariable UUID projectId, @PathVariable UUID runId) {
        return pipelineRunRepository.findById(runId)
                .filter(r -> r.getProjectId().equals(projectId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@PathVariable UUID projectId) {
        return pipelineRunRepository.findByProjectIdOrderByStartedAtDesc(projectId).stream()
                .findFirst()
                .map(run -> ResponseEntity.ok(Map.<String, Object>of(
                        "runId", run.getId().toString(),
                        "currentStep", run.getCurrentStep().name(),
                        "status", run.getStatus().name(),
                        "tokensUsed", run.getTotalTokensUsed()
                )))
                .orElse(ResponseEntity.ok(Map.of("status", "NO_RUNS")));
    }
}
