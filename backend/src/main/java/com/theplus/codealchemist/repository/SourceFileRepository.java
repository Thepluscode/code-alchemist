package com.theplus.codealchemist.repository;

import com.theplus.codealchemist.domain.SourceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SourceFileRepository extends JpaRepository<SourceFile, UUID> {
    List<SourceFile> findByProjectId(UUID projectId);
}
