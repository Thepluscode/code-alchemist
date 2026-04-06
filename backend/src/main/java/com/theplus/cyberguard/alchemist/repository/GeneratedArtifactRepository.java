package com.theplus.cyberguard.alchemist.repository;

import com.theplus.cyberguard.alchemist.domain.GeneratedArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GeneratedArtifactRepository extends JpaRepository<GeneratedArtifact, UUID> {
    List<GeneratedArtifact> findByProjectId(UUID projectId);
    List<GeneratedArtifact> findByProjectIdAndArtifactType(UUID projectId, String artifactType);
}
