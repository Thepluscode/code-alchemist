package com.theplus.codealchemist.domain;

import com.theplus.codealchemist.domain.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "modernization_projects")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ModernizationProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    private String description;

    @Column(name = "source_language")
    private String sourceLanguage;

    @Column(name = "target_stack")
    private String targetStack;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
