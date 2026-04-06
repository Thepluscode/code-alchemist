package com.theplus.codealchemist.controller;

import com.theplus.codealchemist.domain.AlchemistAuditEntry;
import com.theplus.codealchemist.repository.AlchemistAuditEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AlchemistAuditEntryRepository auditRepository;

    @GetMapping
    public List<AlchemistAuditEntry> list(@PathVariable UUID projectId) {
        return auditRepository.findByProjectIdOrderByTimestampDesc(projectId);
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<AlchemistAuditEntry> get(@PathVariable UUID projectId, @PathVariable UUID entryId) {
        return auditRepository.findById(entryId)
                .filter(e -> e.getProjectId().equals(projectId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
