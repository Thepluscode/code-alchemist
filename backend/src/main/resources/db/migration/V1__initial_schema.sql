-- Code Alchemist Service - Initial Schema

CREATE TABLE modernization_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_name VARCHAR(200) NOT NULL,
    description TEXT,
    source_language VARCHAR(50),
    target_stack VARCHAR(200),
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    created_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE source_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES modernization_projects(id),
    filename VARCHAR(500) NOT NULL,
    language VARCHAR(50),
    raw_content TEXT,
    parsed_ast JSONB,
    content_hash VARCHAR(64),
    line_count INT,
    structural_elements JSONB,
    ingested_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE extracted_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES modernization_projects(id),
    source_file_id UUID REFERENCES source_files(id),
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50),
    description TEXT,
    pseudo_code TEXT,
    confidence DOUBLE PRECISION,
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    extracted_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE graph_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES modernization_projects(id),
    node_type VARCHAR(50) NOT NULL,
    name VARCHAR(300) NOT NULL,
    source_file_id UUID REFERENCES source_files(id),
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE graph_edges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES modernization_projects(id),
    source_node_id UUID NOT NULL REFERENCES graph_nodes(id),
    target_node_id UUID NOT NULL REFERENCES graph_nodes(id),
    edge_type VARCHAR(50) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE generated_artifacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES modernization_projects(id),
    artifact_type VARCHAR(50) NOT NULL,
    filename VARCHAR(500),
    content TEXT,
    content_hash VARCHAR(64),
    signature VARCHAR(128),
    generation_model VARCHAR(100),
    generation_prompt_version VARCHAR(20),
    generated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE pipeline_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES modernization_projects(id),
    current_step VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING',
    total_tokens_used INT DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE alchemist_audit_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES modernization_projects(id),
    action VARCHAR(100) NOT NULL,
    actor VARCHAR(100),
    details JSONB,
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_source_files_project ON source_files(project_id);
CREATE INDEX idx_extracted_rules_project ON extracted_rules(project_id);
CREATE INDEX idx_extracted_rules_status ON extracted_rules(project_id, approval_status);
CREATE INDEX idx_graph_nodes_project ON graph_nodes(project_id);
CREATE INDEX idx_graph_edges_project ON graph_edges(project_id);
CREATE INDEX idx_graph_edges_source ON graph_edges(source_node_id);
CREATE INDEX idx_graph_edges_target ON graph_edges(target_node_id);
CREATE INDEX idx_generated_artifacts_project ON generated_artifacts(project_id);
CREATE INDEX idx_generated_artifacts_type ON generated_artifacts(project_id, artifact_type);
CREATE INDEX idx_pipeline_runs_project ON pipeline_runs(project_id);
CREATE INDEX idx_audit_entries_project ON alchemist_audit_entries(project_id);
