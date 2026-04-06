package com.theplus.codealchemist.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "extracted_rules")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ExtractedRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "source_file_id")
    private UUID sourceFileId;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "pseudo_code", columnDefinition = "TEXT")
    private String pseudoCode;

    private Double confidence;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "extracted_at")
    private Instant extractedAt;

    @PrePersist
    void onCreate() {
        extractedAt = Instant.now();
    }
}
