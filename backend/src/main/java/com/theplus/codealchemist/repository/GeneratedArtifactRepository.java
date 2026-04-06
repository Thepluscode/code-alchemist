package com.theplus.codealchemist.repository;

import com.theplus.codealchemist.domain.GeneratedArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GeneratedArtifactRepository extends JpaRepository<GeneratedArtifact, UUID> {
    List<GeneratedArtifact> findByProjectId(UUID projectId);
    List<GeneratedArtifact> findByProjectIdAndArtifactType(UUID projectId, String artifactType);
}
