package com.theplus.codealchemist.controller;

import com.theplus.codealchemist.graph.GraphQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphQueryService graphQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getGraph(@PathVariable UUID projectId) {
        return ResponseEntity.ok(graphQueryService.getFullGraph(projectId));
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(@PathVariable UUID projectId) {
        return ResponseEntity.ok(graphQueryService.getMetrics(projectId));
    }

    @GetMapping("/subgraph/{nodeId}")
    public ResponseEntity<Map<String, Object>> getSubgraph(
            @PathVariable UUID projectId,
            @PathVariable UUID nodeId,
            @RequestParam(defaultValue = "2") int depth) {
        return ResponseEntity.ok(graphQueryService.getSubgraph(projectId, nodeId, depth));
    }
}
