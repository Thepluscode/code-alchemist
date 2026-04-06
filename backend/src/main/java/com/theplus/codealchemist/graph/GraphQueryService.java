package com.theplus.codealchemist.graph;

import com.theplus.codealchemist.domain.GraphEdge;
import com.theplus.codealchemist.domain.GraphNode;
import com.theplus.codealchemist.repository.GraphEdgeRepository;
import com.theplus.codealchemist.repository.GraphNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraphQueryService {

    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;

    public Map<String, Object> getFullGraph(UUID projectId) {
        List<GraphNode> nodes = graphNodeRepository.findByProjectId(projectId);
        List<GraphEdge> edges = graphEdgeRepository.findByProjectId(projectId);
        return Map.of("nodes", nodes, "edges", edges);
    }

    public Map<String, Object> getSubgraph(UUID projectId, UUID nodeId, int depth) {
        List<GraphEdge> allEdges = graphEdgeRepository.findByProjectId(projectId);

        Set<UUID> visited = new HashSet<>();
        Set<UUID> nodeIds = new HashSet<>();
        Queue<UUID> queue = new LinkedList<>();
        queue.add(nodeId);
        nodeIds.add(nodeId);

        for (int d = 0; d < depth && !queue.isEmpty(); d++) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                UUID current = queue.poll();
                if (visited.contains(current)) continue;
                visited.add(current);

                for (GraphEdge e : allEdges) {
                    if (e.getSourceNodeId().equals(current)) {
                        nodeIds.add(e.getTargetNodeId());
                        if (!visited.contains(e.getTargetNodeId())) queue.add(e.getTargetNodeId());
                    }
                    if (e.getTargetNodeId().equals(current)) {
                        nodeIds.add(e.getSourceNodeId());
                        if (!visited.contains(e.getSourceNodeId())) queue.add(e.getSourceNodeId());
                    }
                }
            }
        }

        List<GraphNode> nodes = graphNodeRepository.findAllById(nodeIds).stream().toList();
        List<GraphEdge> edges = allEdges.stream()
                .filter(e -> nodeIds.contains(e.getSourceNodeId()) && nodeIds.contains(e.getTargetNodeId()))
                .toList();

        return Map.of("nodes", nodes, "edges", edges);
    }

    public Map<String, Object> getMetrics(UUID projectId) {
        List<GraphNode> nodes = graphNodeRepository.findByProjectId(projectId);
        List<GraphEdge> edges = graphEdgeRepository.findByProjectId(projectId);

        Map<String, Long> nodesByType = nodes.stream()
                .collect(Collectors.groupingBy(GraphNode::getNodeType, Collectors.counting()));
        Map<String, Long> edgesByType = edges.stream()
                .collect(Collectors.groupingBy(GraphEdge::getEdgeType, Collectors.counting()));

        // Coupling: ratio of edges to nodes (higher = more coupled)
        double coupling = nodes.isEmpty() ? 0.0 : (double) edges.size() / nodes.size();

        // Find most connected nodes
        Map<UUID, Long> connectionCounts = new HashMap<>();
        edges.forEach(e -> {
            connectionCounts.merge(e.getSourceNodeId(), 1L, Long::sum);
            connectionCounts.merge(e.getTargetNodeId(), 1L, Long::sum);
        });

        List<Map<String, Object>> mostConnected = connectionCounts.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    GraphNode node = nodes.stream().filter(n -> n.getId().equals(entry.getKey())).findFirst().orElse(null);
                    return Map.<String, Object>of(
                            "nodeId", entry.getKey().toString(),
                            "name", node != null ? node.getName() : "unknown",
                            "connections", entry.getValue()
                    );
                })
                .toList();

        return Map.of(
                "totalNodes", nodes.size(),
                "totalEdges", edges.size(),
                "nodesByType", nodesByType,
                "edgesByType", edgesByType,
                "couplingScore", Math.round(coupling * 100.0) / 100.0,
                "mostConnectedNodes", mostConnected
        );
    }
}
