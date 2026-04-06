package com.theplus.cyberguard.alchemist.audit;

import com.theplus.cyberguard.alchemist.domain.AlchemistAuditEntry;
import com.theplus.cyberguard.alchemist.repository.AlchemistAuditEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlchemistAuditLogger {

    private final AlchemistAuditEntryRepository auditRepository;
    private final ObjectMapper objectMapper;

    public void log(UUID projectId, String action, String actor, Map<String, Object> details) {
        try {
            String detailsJson = objectMapper.writeValueAsString(details);
            AlchemistAuditEntry entry = AlchemistAuditEntry.builder()
                    .projectId(projectId)
                    .action(action)
                    .actor(actor)
                    .details(detailsJson)
                    .build();
            auditRepository.save(entry);
            log.info("Audit: project={} action={} actor={}", projectId, action, actor);
        } catch (Exception e) {
            log.error("Failed to write audit entry: {}", e.getMessage());
        }
    }
}
