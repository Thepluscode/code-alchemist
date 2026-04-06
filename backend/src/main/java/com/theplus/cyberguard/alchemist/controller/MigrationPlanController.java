package com.theplus.cyberguard.alchemist.controller;

import com.theplus.cyberguard.alchemist.migration.MigrationPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/migration-plan")
@RequiredArgsConstructor
public class MigrationPlanController {

    private final MigrationPlanService migrationPlanService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> generate(@PathVariable UUID projectId) {
        try {
            Map<String, Object> result = migrationPlanService.plan(projectId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
