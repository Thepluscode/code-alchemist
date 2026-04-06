package com.theplus.cyberguard.alchemist.repository;

import com.theplus.cyberguard.alchemist.domain.GraphNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GraphNodeRepository extends JpaRepository<GraphNode, UUID> {
    List<GraphNode> findByProjectId(UUID projectId);
    List<GraphNode> findByProjectIdAndNodeType(UUID projectId, String nodeType);
}
