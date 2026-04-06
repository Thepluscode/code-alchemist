package com.theplus.cyberguard.alchemist.repository;

import com.theplus.cyberguard.alchemist.domain.GraphEdge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GraphEdgeRepository extends JpaRepository<GraphEdge, UUID> {
    List<GraphEdge> findByProjectId(UUID projectId);
    List<GraphEdge> findBySourceNodeIdOrTargetNodeId(UUID sourceNodeId, UUID targetNodeId);
}
