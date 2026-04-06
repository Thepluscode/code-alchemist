package com.theplus.codealchemist.controller;

import com.theplus.codealchemist.domain.GeneratedArtifact;
import com.theplus.codealchemist.repository.GeneratedArtifactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/artifacts")
@RequiredArgsConstructor
public class ArtifactController {

    private final GeneratedArtifactRepository artifactRepository;

    @GetMapping
    public List<GeneratedArtifact> list(@PathVariable UUID projectId,
                                         @RequestParam(required = false) String type) {
        if (type != null) {
            return artifactRepository.findByProjectIdAndArtifactType(projectId, type);
        }
        return artifactRepository.findByProjectId(projectId);
    }

    @GetMapping("/{artifactId}")
    public ResponseEntity<GeneratedArtifact> get(@PathVariable UUID projectId, @PathVariable UUID artifactId) {
        return artifactRepository.findById(artifactId)
                .filter(a -> a.getProjectId().equals(projectId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{artifactId}/content")
    public ResponseEntity<String> getContent(@PathVariable UUID projectId, @PathVariable UUID artifactId) {
        return artifactRepository.findById(artifactId)
                .filter(a -> a.getProjectId().equals(projectId))
                .map(a -> ResponseEntity.ok(a.getContent()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{artifactId}")
    public ResponseEntity<Void> delete(@PathVariable UUID projectId, @PathVariable UUID artifactId) {
        return artifactRepository.findById(artifactId)
                .filter(a -> a.getProjectId().equals(projectId))
                .map(a -> {
                    artifactRepository.delete(a);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
