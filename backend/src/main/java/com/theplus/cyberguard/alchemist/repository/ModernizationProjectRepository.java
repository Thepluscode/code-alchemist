package com.theplus.cyberguard.alchemist.repository;

import com.theplus.cyberguard.alchemist.domain.ModernizationProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ModernizationProjectRepository extends JpaRepository<ModernizationProject, UUID> {
}
