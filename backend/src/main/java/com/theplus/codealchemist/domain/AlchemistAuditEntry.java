package com.theplus.codealchemist.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String details;

    @Column(name = "timestamp")
    private Instant timestamp;

    @PrePersist
    void onCreate() {
        timestamp = Instant.now();
    }
}
