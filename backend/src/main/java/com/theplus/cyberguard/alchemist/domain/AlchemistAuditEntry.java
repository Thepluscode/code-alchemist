package com.theplus.cyberguard.alchemist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alchemist_audit_entries")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AlchemistAuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String action;

    private String actor;

    @Column(columnDefinition = "jsonb")
    private String details;

    @Column(name = "timestamp")
    private Instant timestamp;

    @PrePersist
    void onCreate() {
        timestamp = Instant.now();
    }
}
