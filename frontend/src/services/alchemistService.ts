import { http } from '@/config/axios';
import { API } from '@/config/api';

// ── Projects ──────────────────────────────────────────────────────────────────

export async function fetchProjects() {
  const { data } = await http.get(API.PROJECTS);
  return data;
}

export async function createProject(body: {
  projectName: string;
  description?: string;
  sourceLanguage?: string;
  targetStack?: string;
}) {
  const { data } = await http.post(API.PROJECTS, body);
  return data;
}

export async function fetchProject(id: string) {
  const { data } = await http.get(API.PROJECT_DETAIL(id));
  return data;
}

export async function deleteProject(id: string) {
  await http.delete(API.PROJECT_DETAIL(id));
}

// ── Files ─────────────────────────────────────────────────────────────────────

export async function fetchFiles(projectId: string) {
  const { data } = await http.get(API.FILES(projectId));
  return data;
}

export async function uploadFiles(projectId: string, files: File[]) {
  const formData = new FormData();
  files.forEach((f) => formData.append('files', f));
  const { data } = await http.post(API.FILES_UPLOAD(projectId), formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function inlineUpload(projectId: string, filename: string, content: string) {
  const { data } = await http.post(API.FILES_INLINE(projectId), { filename, content });
  return data;
}

// ── Pipeline ──────────────────────────────────────────────────────────────────

export async function startPipeline(projectId: string) {
  const { data } = await http.post(API.PIPELINE_START(projectId));
  return data;
}

export async function resumePipeline(projectId: string) {
  const { data } = await http.post(API.PIPELINE_RESUME(projectId));
  return data;
}

export async function fetchPipelineStatus(projectId: string) {
  const { data } = await http.get(API.PIPELINE_STATUS(projectId));
  return data;
}

export async function fetchPipelineRuns(projectId: string) {
  const { data } = await http.get(API.PIPELINE_RUNS(projectId));
  return data;
}

// ── Rules ─────────────────────────────────────────────────────────────────────

export async function fetchRules(projectId: string, status?: string) {
  const { data } = await http.get(API.RULES(projectId), {
    params: status ? { status } : undefined,
  });
  return data;
}

export async function approveRule(projectId: string, ruleId: string) {
  const { data } = await http.patch(API.RULE_APPROVE(projectId, ruleId));
  return data;
}

export async function rejectRule(projectId: string, ruleId: string) {
  const { data } = await http.patch(API.RULE_REJECT(projectId, ruleId));
  return data;
}

export async function approveAllRules(projectId: string) {
  const { data } = await http.patch(API.RULES_APPROVE_ALL(projectId));
  return data;
}

// ── Graph ─────────────────────────────────────────────────────────────────────

export async function fetchGraph(projectId: string) {
  const { data } = await http.get(API.GRAPH(projectId));
  return data;
}

export async function fetchGraphMetrics(projectId: string) {
  const { data } = await http.get(API.GRAPH_METRICS(projectId));
  return data;
}

// ── Artifacts ─────────────────────────────────────────────────────────────────

export async function fetchArtifacts(projectId: string, type?: string) {
  const { data } = await http.get(API.ARTIFACTS(projectId), {
    params: type ? { type } : undefined,
  });
  return data;
}

export async function fetchArtifactContent(projectId: string, artifactId: string) {
  const { data } = await http.get(API.ARTIFACT_CONTENT(projectId, artifactId));
  return data;
}

// ── Migration Plan ────────────────────────────────────────────────────────────

export async function generateMigrationPlan(projectId: string) {
  const { data } = await http.post(API.MIGRATION_PLAN(projectId));
  return data;
}

// ── Audit ─────────────────────────────────────────────────────────────────────

export async function fetchAuditEntries(projectId: string) {
  const { data } = await http.get(API.AUDIT(projectId));
  return data;
}
