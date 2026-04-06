package com.theplus.codealchemist.repository;

import com.theplus.codealchemist.domain.ModernizationProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ModernizationProjectRepository extends JpaRepository<ModernizationProject, UUID> {
}
