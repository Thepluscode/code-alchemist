package com.theplus.cyberguard.alchemist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "source_files")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String filename;

    private String language;

    @Column(name = "raw_content", columnDefinition = "TEXT")
    private String rawContent;

    @Column(name = "parsed_ast", columnDefinition = "jsonb")
    private String parsedAst;

    @Column(name = "content_hash")
    private String contentHash;

    @Column(name = "line_count")
    private Integer lineCount;

    @Column(name = "structural_elements", columnDefinition = "jsonb")
    private String structuralElements;

    @Column(name = "ingested_at")
    private Instant ingestedAt;

    @PrePersist
    void onCreate() {
        ingestedAt = Instant.now();
    }
}
