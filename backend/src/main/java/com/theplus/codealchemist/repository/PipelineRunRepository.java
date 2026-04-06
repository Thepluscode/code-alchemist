package com.theplus.codealchemist.repository;

import com.theplus.codealchemist.domain.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, UUID> {
    List<PipelineRun> findByProjectIdOrderByStartedAtDesc(UUID projectId);
}
