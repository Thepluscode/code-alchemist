package com.theplus.cyberguard.alchemist.repository;

import com.theplus.cyberguard.alchemist.domain.SourceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SourceFileRepository extends JpaRepository<SourceFile, UUID> {
    List<SourceFile> findByProjectId(UUID projectId);
}
