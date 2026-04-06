package com.theplus.codealchemist.repository;

import com.theplus.codealchemist.domain.AlchemistAuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlchemistAuditEntryRepository extends JpaRepository<AlchemistAuditEntry, UUID> {
    List<AlchemistAuditEntry> findByProjectIdOrderByTimestampDesc(UUID projectId);
}
