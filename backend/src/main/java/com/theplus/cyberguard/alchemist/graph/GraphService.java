package com.theplus.cyberguard.alchemist.graph;

import com.theplus.cyberguard.alchemist.domain.ExtractedRule;
import com.theplus.cyberguard.alchemist.domain.GraphEdge;
import com.theplus.cyberguard.alchemist.domain.GraphNode;
import com.theplus.cyberguard.alchemist.domain.SourceFile;
import com.theplus.cyberguard.alchemist.repository.ExtractedRuleRepository;
import com.theplus.cyberguard.alchemist.repository.GraphEdgeRepository;
import com.theplus.cyberguard.alchemist.repository.GraphNodeRepository;
import com.theplus.cyberguard.alchemist.repository.SourceFileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GraphService {

    private final SourceFileRepository sourceFileRepository;
    private final ExtractedRuleRepository extractedRuleRepository;
    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;
    private final ObjectMapper objectMapper;

    public Map<String, Object> buildGraph(UUID projectId) {
        List<SourceFile> files = sourceFileRepository.findByProjectId(projectId);

        Map<String, UUID> nodeIndex = new HashMap<>();
        int nodeCount = 0;
        int edgeCount = 0;

        // Create program nodes for each source file
        for (SourceFile file : files) {
            GraphNode programNode = GraphNode.builder()
                    .projectId(projectId)
                    .nodeType("PROGRAM")
                    .name(file.getFilename())
                    .metadata("{\"language\":\"" + file.getLanguage() + "\"}")
                    .sourceFileId(file.getId())
                    .build();
            programNode = graphNodeRepository.save(programNode);
            nodeIndex.put("PROGRAM:" + file.getFilename(), programNode.getId());
            nodeCount++;

            // Parse AST for structural elements
            try {
                Map<String, Object> ast = file.getParsedAst() != null
                        ? objectMapper.readValue(file.getParsedAst(), new TypeReference<>() {})
                        : Map.of();

                // Create nodes for subroutines/paragraphs/procedures
                List<String> procedures = extractList(ast, "paragraphs", "procedures", "subroutines");
                for (String proc : procedures) {
                    GraphNode subNode = GraphNode.builder()
                            .projectId(projectId)
                            .nodeType("SUBROUTINE")
                            .name(proc)
                            .metadata("{}")
                            .sourceFileId(file.getId())
                            .build();
                    subNode = graphNodeRepository.save(subNode);
                    nodeIndex.put("SUB:" + file.getFilename() + ":" + proc, subNode.getId());
                    nodeCount++;

                    // CONTAINS edge: program -> subroutine
                    createEdge(projectId, programNode.getId(), subNode.getId(), "CONTAINS");
                    edgeCount++;
                }

                // Create nodes for external calls and CALLS edges
                List<String> calls = extractList(ast, "externalCalls");
                for (String call : calls) {
                    String callKey = "EXTERNAL_CALL:" + call;
                    UUID callNodeId = nodeIndex.computeIfAbsent(callKey, k -> {
                        GraphNode n = GraphNode.builder()
                                .projectId(projectId).nodeType("EXTERNAL_CALL").name(call).metadata("{}").build();
                        return graphNodeRepository.save(n).getId();
                    });
                    createEdge(projectId, programNode.getId(), callNodeId, "CALLS");
                    edgeCount++;
                }

                // Create nodes for copybooks/includes and INCLUDES edges
                List<String> copies = extractList(ast, "copybooks");
                for (String copy : copies) {
                    String copyKey = "COPYBOOK:" + copy;
                    UUID copyNodeId = nodeIndex.computeIfAbsent(copyKey, k -> {
                        GraphNode n = GraphNode.builder()
                                .projectId(projectId).nodeType("COPYBOOK").name(copy).metadata("{}").build();
                        return graphNodeRepository.save(n).getId();
                    });
                    createEdge(projectId, programNode.getId(), copyNodeId, "INCLUDES");
                    edgeCount++;
                }

                // Create nodes for file operations
                List<String> fileOps = extractList(ast, "fileOperations");
                for (String fileOp : fileOps) {
                    String fileKey = "FILE_IO:" + fileOp;
                    UUID fileNodeId = nodeIndex.computeIfAbsent(fileKey, k -> {
                        GraphNode n = GraphNode.builder()
                                .projectId(projectId).nodeType("FILE_IO").name(fileOp).metadata("{}").build();
                        return graphNodeRepository.save(n).getId();
                    });
                    createEdge(projectId, programNode.getId(), fileNodeId, "READS");
                    edgeCount++;
                }

            } catch (Exception e) {
                log.warn("Failed to build graph from AST for {}: {}", file.getFilename(), e.getMessage());
            }
        }

        // Create nodes for extracted business rules
        List<ExtractedRule> rules = extractedRuleRepository.findByProjectId(projectId);
        for (ExtractedRule rule : rules) {
            GraphNode ruleNode = GraphNode.builder()
                    .projectId(projectId)
                    .nodeType("BUSINESS_RULE")
                    .name(rule.getRuleName())
                    .metadata("{\"type\":\"" + rule.getRuleType() + "\",\"confidence\":" + rule.getConfidence() + "}")
                    .sourceFileId(rule.getSourceFileId())
                    .build();
            ruleNode = graphNodeRepository.save(ruleNode);
            nodeCount++;

            // Link rule to its source file's program node
            if (rule.getSourceFileId() != null) {
                SourceFile sf = files.stream().filter(f -> f.getId().equals(rule.getSourceFileId())).findFirst().orElse(null);
                if (sf != null) {
                    UUID programNodeId = nodeIndex.get("PROGRAM:" + sf.getFilename());
                    if (programNodeId != null) {
                        createEdge(projectId, programNodeId, ruleNode.getId(), "CONTAINS");
                        edgeCount++;
                    }
                }
            }
        }

        log.info("Built knowledge graph for project {}: {} nodes, {} edges", projectId, nodeCount, edgeCount);

        return Map.of(
                "nodeCount", nodeCount,
                "edgeCount", edgeCount,
                "nodeTypes", countNodeTypes(projectId)
        );
    }

    private void createEdge(UUID projectId, UUID sourceId, UUID targetId, String edgeType) {
        GraphEdge edge = GraphEdge.builder()
                .projectId(projectId)
                .sourceNodeId(sourceId)
                .targetNodeId(targetId)
                .edgeType(edgeType)
                .metadata("{}")
                .build();
        graphEdgeRepository.save(edge);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractList(Map<String, Object> ast, String... keys) {
        for (String key : keys) {
            Object val = ast.get(key);
            if (val instanceof List<?> list && !list.isEmpty()) {
                return (List<String>) list;
            }
        }
        return List.of();
    }

    private Map<String, Long> countNodeTypes(UUID projectId) {
        Map<String, Long> counts = new HashMap<>();
        graphNodeRepository.findByProjectId(projectId)
                .forEach(n -> counts.merge(n.getNodeType(), 1L, Long::sum));
        return counts;
    }
}
