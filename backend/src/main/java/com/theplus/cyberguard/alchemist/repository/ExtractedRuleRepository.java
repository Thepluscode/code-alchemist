package com.theplus.cyberguard.alchemist.repository;

import com.theplus.cyberguard.alchemist.domain.ExtractedRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExtractedRuleRepository extends JpaRepository<ExtractedRule, UUID> {
    List<ExtractedRule> findByProjectId(UUID projectId);
    List<ExtractedRule> findByProjectIdAndApprovalStatus(UUID projectId, String approvalStatus);
}
