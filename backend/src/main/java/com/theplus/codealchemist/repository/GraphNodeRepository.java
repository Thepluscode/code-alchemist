package com.theplus.codealchemist.repository;

import com.theplus.codealchemist.domain.GraphNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GraphNodeRepository extends JpaRepository<GraphNode, UUID> {
    List<GraphNode> findByProjectId(UUID projectId);
    List<GraphNode> findByProjectIdAndNodeType(UUID projectId, String nodeType);
}
