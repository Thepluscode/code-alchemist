export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '';

const api = (path: string) => `${API_BASE_URL}/api/v1/projects${path}`;

export const API = {
  PROJECTS: api(''),
  PROJECT_DETAIL: (id: string) => api(`/${id}`),
  FILES: (projectId: string) => api(`/${projectId}/files`),
  FILES_UPLOAD: (projectId: string) => api(`/${projectId}/files/upload`),
  FILES_INLINE: (projectId: string) => api(`/${projectId}/files/inline`),
  PIPELINE_START: (projectId: string) => api(`/${projectId}/pipeline/start`),
  PIPELINE_RESUME: (projectId: string) => api(`/${projectId}/pipeline/resume`),
  PIPELINE_STATUS: (projectId: string) => api(`/${projectId}/pipeline/status`),
  PIPELINE_RUNS: (projectId: string) => api(`/${projectId}/pipeline/runs`),
  RULES: (projectId: string) => api(`/${projectId}/rules`),
  RULE_APPROVE: (projectId: string, ruleId: string) => api(`/${projectId}/rules/${ruleId}/approve`),
  RULE_REJECT: (projectId: string, ruleId: string) => api(`/${projectId}/rules/${ruleId}/reject`),
  RULES_APPROVE_ALL: (projectId: string) => api(`/${projectId}/rules/approve-all`),
  GRAPH: (projectId: string) => api(`/${projectId}/graph`),
  GRAPH_METRICS: (projectId: string) => api(`/${projectId}/graph/metrics`),
  ARTIFACTS: (projectId: string) => api(`/${projectId}/artifacts`),
  ARTIFACT_CONTENT: (projectId: string, artifactId: string) => api(`/${projectId}/artifacts/${artifactId}/content`),
  MIGRATION_PLAN: (projectId: string) => api(`/${projectId}/migration-plan`),
  AUDIT: (projectId: string) => api(`/${projectId}/audit`),
};
