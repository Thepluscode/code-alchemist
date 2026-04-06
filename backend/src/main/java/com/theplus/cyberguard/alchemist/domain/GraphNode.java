package com.theplus.cyberguard.alchemist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "graph_nodes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GraphNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "node_type", nullable = false)
    private String nodeType;

    @Column(nullable = false)
    private String name;

    @Column(name = "source_file_id")
    private UUID sourceFileId;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
