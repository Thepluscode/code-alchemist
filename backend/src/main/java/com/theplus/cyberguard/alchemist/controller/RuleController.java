package com.theplus.cyberguard.alchemist.controller;

import com.theplus.cyberguard.alchemist.domain.ExtractedRule;
import com.theplus.cyberguard.alchemist.repository.ExtractedRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/rules")
@RequiredArgsConstructor
public class RuleController {

    private final ExtractedRuleRepository ruleRepository;

    @GetMapping
    public List<ExtractedRule> list(@PathVariable UUID projectId,
                                    @RequestParam(required = false) String status) {
        if (status != null) {
            return ruleRepository.findByProjectIdAndApprovalStatus(projectId, status);
        }
        return ruleRepository.findByProjectId(projectId);
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<ExtractedRule> get(@PathVariable UUID projectId, @PathVariable UUID ruleId) {
        return ruleRepository.findById(ruleId)
                .filter(r -> r.getProjectId().equals(projectId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{ruleId}/approve")
    public ResponseEntity<ExtractedRule> approve(@PathVariable UUID projectId, @PathVariable UUID ruleId,
                                                  @RequestBody(required = false) Map<String, String> body) {
        return ruleRepository.findById(ruleId)
                .filter(r -> r.getProjectId().equals(projectId))
                .map(rule -> {
                    rule.setApprovalStatus("APPROVED");
                    rule.setApprovedBy(body != null ? body.getOrDefault("approvedBy", "analyst") : "analyst");
                    return ResponseEntity.ok(ruleRepository.save(rule));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{ruleId}/reject")
    public ResponseEntity<ExtractedRule> reject(@PathVariable UUID projectId, @PathVariable UUID ruleId,
                                                 @RequestBody(required = false) Map<String, String> body) {
        return ruleRepository.findById(ruleId)
                .filter(r -> r.getProjectId().equals(projectId))
                .map(rule -> {
                    rule.setApprovalStatus("REJECTED");
                    return ResponseEntity.ok(ruleRepository.save(rule));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/approve-all")
    public ResponseEntity<Map<String, Object>> approveAll(@PathVariable UUID projectId) {
        List<ExtractedRule> pending = ruleRepository.findByProjectIdAndApprovalStatus(projectId, "PENDING");
        for (ExtractedRule rule : pending) {
            rule.setApprovalStatus("APPROVED");
            rule.setApprovedBy("bulk-approve");
        }
        ruleRepository.saveAll(pending);
        return ResponseEntity.ok(Map.of("approved", pending.size()));
    }

    @PatchMapping("/{ruleId}")
    public ResponseEntity<ExtractedRule> update(@PathVariable UUID projectId, @PathVariable UUID ruleId,
                                                 @RequestBody Map<String, String> body) {
        return ruleRepository.findById(ruleId)
                .filter(r -> r.getProjectId().equals(projectId))
                .map(rule -> {
                    if (body.containsKey("ruleName")) rule.setRuleName(body.get("ruleName"));
                    if (body.containsKey("description")) rule.setDescription(body.get("description"));
                    if (body.containsKey("pseudoCode")) rule.setPseudoCode(body.get("pseudoCode"));
                    return ResponseEntity.ok(ruleRepository.save(rule));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
