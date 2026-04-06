export interface ModernizationProject {
  id: string;
  projectName: string;
  description: string;
  sourceLanguage: string;
  targetStack: string;
  status: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface AlchemistSourceFile {
  id: string;
  projectId: string;
  filename: string;
  language: string;
  rawContent: string;
  lineCount: number;
  ingestedAt: string;
}

export interface ExtractedRule {
  id: string;
  projectId: string;
  ruleName: string;
  ruleType: string;
  description: string;
  pseudoCode: string;
  confidence: number;
  approvalStatus: string;
  approvedBy: string;
  extractedAt: string;
}

export interface GeneratedArtifact {
  id: string;
  projectId: string;
  artifactType: string;
  filename: string;
  content: string;
  generationModel: string;
  generatedAt: string;
}

export interface PipelineRun {
  id: string;
  projectId: string;
  currentStep: string;
  status: string;
  totalTokensUsed: number;
  errorMessage: string;
  startedAt: string;
  completedAt: string;
}

export interface PipelineStatus {
  runId?: string;
  currentStep?: string;
  status: string;
  tokensUsed?: number;
}

export interface GraphData {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

export interface GraphNode {
  id: string;
  nodeType: string;
  name: string;
  metadata: Record<string, unknown>;
}

export interface GraphEdge {
  id: string;
  sourceNodeId: string;
  targetNodeId: string;
  edgeType: string;
}

export interface AuditEntry {
  id: string;
  projectId: string;
  action: string;
  actor: string;
  details: Record<string, unknown>;
  timestamp: string;
}
