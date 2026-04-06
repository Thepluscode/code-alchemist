package com.theplus.codealchemist.controller;

import com.theplus.codealchemist.domain.ModernizationProject;
import com.theplus.codealchemist.domain.enums.ProjectStatus;
import com.theplus.codealchemist.repository.ModernizationProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ModernizationProjectRepository projectRepository;

    @PostMapping
    public ResponseEntity<ModernizationProject> create(@RequestBody Map<String, String> body) {
        ModernizationProject project = ModernizationProject.builder()
                .projectName(body.getOrDefault("projectName", "Untitled Project"))
                .description(body.get("description"))
                .sourceLanguage(body.getOrDefault("sourceLanguage", "COBOL"))
                .targetStack(body.getOrDefault("targetStack", "Spring Boot 3 + Java 21"))
                .status(ProjectStatus.CREATED)
                .createdBy(body.getOrDefault("createdBy", "system"))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(projectRepository.save(project));
    }

    @GetMapping
    public List<ModernizationProject> list() {
        return projectRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModernizationProject> get(@PathVariable UUID id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ModernizationProject> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return projectRepository.findById(id).map(project -> {
            if (body.containsKey("projectName")) project.setProjectName(body.get("projectName"));
            if (body.containsKey("description")) project.setDescription(body.get("description"));
            if (body.containsKey("status")) project.setStatus(ProjectStatus.valueOf(body.get("status")));
            return ResponseEntity.ok(projectRepository.save(project));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
