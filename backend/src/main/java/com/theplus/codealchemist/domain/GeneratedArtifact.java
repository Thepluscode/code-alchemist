package com.theplus.codealchemist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "generated_artifacts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GeneratedArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "artifact_type", nullable = false)
    private String artifactType;

    private String filename;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_hash")
    private String contentHash;

    private String signature;

    @Column(name = "generation_model")
    private String generationModel;

    @Column(name = "generation_prompt_version")
    private String generationPromptVersion;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @PrePersist
    void onCreate() {
        generatedAt = Instant.now();
    }
}
