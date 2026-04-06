package com.theplus.codealchemist.pipeline;

import com.theplus.codealchemist.architecture.ArchitectureService;
import com.theplus.codealchemist.audit.AlchemistAuditLogger;
import com.theplus.codealchemist.codegen.CodeGenerationService;
import com.theplus.codealchemist.domain.ModernizationProject;
import com.theplus.codealchemist.domain.PipelineRun;
import com.theplus.codealchemist.domain.enums.PipelineStatus;
import com.theplus.codealchemist.domain.enums.PipelineStep;
import com.theplus.codealchemist.domain.enums.ProjectStatus;
import com.theplus.codealchemist.event.PipelineEventPublisher;
import com.theplus.codealchemist.extraction.ExtractionService;
import com.theplus.codealchemist.graph.GraphService;
import com.theplus.codealchemist.ingestion.IngestionService;
import com.theplus.codealchemist.migration.MigrationPlanService;
import com.theplus.codealchemist.repository.ModernizationProjectRepository;
import com.theplus.codealchemist.repository.PipelineRunRepository;
import com.theplus.codealchemist.validation.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModernizationPipeline {

    private final IngestionService ingestionService;
    private final ExtractionService extractionService;
    private final GraphService graphService;
    private final ArchitectureService architectureService;
    private final CodeGenerationService codeGenerationService;
    private final ValidationService validationService;
    private final MigrationPlanService migrationPlanService;
    private final PipelineRunRepository pipelineRunRepository;
    private final ModernizationProjectRepository projectRepository;
    private final AlchemistAuditLogger auditLogger;
    private final PipelineEventPublisher eventPublisher;

    @Async
    public void startPipeline(UUID projectId) {
        ModernizationProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        PipelineRun run = PipelineRun.builder()
                .projectId(projectId)
                .currentStep(PipelineStep.INGESTION)
                .status(PipelineStatus.RUNNING)
                .totalTokensUsed(0)
                .build();
        run = pipelineRunRepository.save(run);

        project.setStatus(ProjectStatus.INGESTING);
        projectRepository.save(project);

        auditLogger.log(projectId, "PIPELINE_STARTED", "system",
                Map.of("runId", run.getId().toString(), "message", "Pipeline initiated"));
        publishUpdate(projectId, PipelineStep.INGESTION, PipelineStatus.RUNNING);

        try {
            // Step 1: Ingestion
            Map<String, Object> ingestionResult = ingestionService.ingest(projectId);
            publishUpdate(projectId, PipelineStep.INGESTION, PipelineStatus.COMPLETED);

            // Step 2: Extraction (LLM)
            run.setCurrentStep(PipelineStep.EXTRACTION);
            project.setStatus(ProjectStatus.EXTRACTING);
            projectRepository.save(project);
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.EXTRACTION, PipelineStatus.RUNNING);

            Map<String, Object> extractionResult = extractionService.extract(projectId);
            run.setTotalTokensUsed(run.getTotalTokensUsed() + getInt(extractionResult, "tokensUsed"));
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.EXTRACTION, PipelineStatus.COMPLETED);

            // Step 3: Graph Building
            run.setCurrentStep(PipelineStep.GRAPH_BUILDING);
            project.setStatus(ProjectStatus.GRAPH_BUILDING);
            projectRepository.save(project);
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.GRAPH_BUILDING, PipelineStatus.RUNNING);

            Map<String, Object> graphResult = graphService.buildGraph(projectId);
            publishUpdate(projectId, PipelineStep.GRAPH_BUILDING, PipelineStatus.COMPLETED);

            // Step 4: Pause for human approval
            run.setCurrentStep(PipelineStep.AWAITING_APPROVAL);
            run.setStatus(PipelineStatus.PAUSED);
            pipelineRunRepository.save(run);

            project.setStatus(ProjectStatus.AWAITING_APPROVAL);
            projectRepository.save(project);

            auditLogger.log(projectId, "AWAITING_APPROVAL", "system",
                    Map.of("runId", run.getId().toString(),
                            "message", "Pipeline paused — extracted rules require human review"));
            publishUpdate(projectId, PipelineStep.AWAITING_APPROVAL, PipelineStatus.PAUSED);

            // Pipeline pauses here. resumePipeline() continues from step 5.

        } catch (Exception e) {
            handleFailure(run, project, e);
        }
    }

    @Async
    public void resumePipeline(UUID projectId) {
        PipelineRun run = pipelineRunRepository.findByProjectIdOrderByStartedAtDesc(projectId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pipeline run found for project: " + projectId));

        if (run.getStatus() != PipelineStatus.PAUSED) {
            throw new IllegalStateException("Pipeline is not paused, current status: " + run.getStatus());
        }

        ModernizationProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        run.setStatus(PipelineStatus.RUNNING);
        project.setStatus(ProjectStatus.DESIGNING);
        projectRepository.save(project);

        auditLogger.log(projectId, "PIPELINE_RESUMED", "system",
                Map.of("runId", run.getId().toString(), "message", "Resuming after rule approval"));

        try {
            // Step 5: Architecture Design (LLM)
            run.setCurrentStep(PipelineStep.ARCHITECTURE_DESIGN);
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.ARCHITECTURE_DESIGN, PipelineStatus.RUNNING);

            Map<String, Object> archResult = architectureService.design(projectId);
            run.setTotalTokensUsed(run.getTotalTokensUsed() + getInt(archResult, "tokensUsed"));
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.ARCHITECTURE_DESIGN, PipelineStatus.COMPLETED);

            // Step 6: Code Generation (LLM)
            run.setCurrentStep(PipelineStep.CODE_GENERATION);
            project.setStatus(ProjectStatus.GENERATING);
            projectRepository.save(project);
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.CODE_GENERATION, PipelineStatus.RUNNING);

            Map<String, Object> codegenResult = codeGenerationService.generate(projectId);
            run.setTotalTokensUsed(run.getTotalTokensUsed() + getInt(codegenResult, "tokensUsed"));
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.CODE_GENERATION, PipelineStatus.COMPLETED);

            // Step 7: Validation (LLM)
            run.setCurrentStep(PipelineStep.VALIDATION);
            project.setStatus(ProjectStatus.VALIDATING);
            projectRepository.save(project);
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.VALIDATION, PipelineStatus.RUNNING);

            Map<String, Object> validationResult = validationService.validate(projectId);
            run.setTotalTokensUsed(run.getTotalTokensUsed() + getInt(validationResult, "tokensUsed"));
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.VALIDATION, PipelineStatus.COMPLETED);

            // Step 8: Migration Planning (LLM)
            run.setCurrentStep(PipelineStep.MIGRATION_PLANNING);
            project.setStatus(ProjectStatus.PLANNING_MIGRATION);
            projectRepository.save(project);
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.MIGRATION_PLANNING, PipelineStatus.RUNNING);

            Map<String, Object> migrationResult = migrationPlanService.plan(projectId);
            run.setTotalTokensUsed(run.getTotalTokensUsed() + getInt(migrationResult, "tokensUsed"));
            pipelineRunRepository.save(run);
            publishUpdate(projectId, PipelineStep.MIGRATION_PLANNING, PipelineStatus.COMPLETED);

            // Done
            run.setCurrentStep(PipelineStep.COMPLETED);
            run.setStatus(PipelineStatus.COMPLETED);
            run.setCompletedAt(Instant.now());
            pipelineRunRepository.save(run);

            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);

            auditLogger.log(projectId, "PIPELINE_COMPLETED", "system",
                    Map.of("runId", run.getId().toString(),
                            "message", String.format("Total LLM tokens: %d", run.getTotalTokensUsed())));
            publishUpdate(projectId, PipelineStep.COMPLETED, PipelineStatus.COMPLETED);

        } catch (Exception e) {
            handleFailure(run, project, e);
        }
    }

    private void handleFailure(PipelineRun run, ModernizationProject project, Exception e) {
        log.error("Pipeline failed at step {} for project {}: {}",
                run.getCurrentStep(), run.getProjectId(), e.getMessage(), e);
        run.setStatus(PipelineStatus.FAILED);
        run.setErrorMessage(e.getMessage());
        run.setCompletedAt(Instant.now());
        pipelineRunRepository.save(run);

        project.setStatus(ProjectStatus.FAILED);
        projectRepository.save(project);

        auditLogger.log(run.getProjectId(), "PIPELINE_FAILED", "system",
                Map.of("runId", run.getId().toString(),
                        "message", "Failed at " + run.getCurrentStep() + ": " + e.getMessage()));
        publishUpdate(run.getProjectId(), run.getCurrentStep(), PipelineStatus.FAILED);
    }

    private int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof Number n ? n.intValue() : 0;
    }

    private void publishUpdate(UUID projectId, PipelineStep step, PipelineStatus status) {
        try {
            eventPublisher.publishStepUpdate(projectId, step.name(), status.name(), Map.of());
        } catch (Exception e) {
            log.warn("Failed to publish pipeline event: {}", e.getMessage());
        }
    }
}
