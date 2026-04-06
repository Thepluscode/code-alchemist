import React, { useEffect, useState, useCallback } from 'react';
import {
  Container, Box, Typography, Tabs, Tab, Button, Chip, Stepper, Step, StepLabel,
  Table, TableHead, TableRow, TableCell, TableBody, Card, CardContent,
  CircularProgress, Alert, IconButton, Paper,
} from '@mui/material';
import {
  PlayArrow, CheckCircle, Cancel, DoneAll, Upload, AutoFixHigh, ArrowBack,
} from '@mui/icons-material';
import { useRouter } from 'next/router';
import {
  fetchProject, fetchFiles, uploadFiles, fetchPipelineStatus, startPipeline,
  resumePipeline, fetchRules, approveRule, rejectRule, approveAllRules,
  fetchGraph, fetchArtifacts, fetchAuditEntries,
} from '@/services/alchemistService';
import type { ModernizationProject, AlchemistSourceFile, ExtractedRule, GeneratedArtifact, AuditEntry } from '@/types/alchemist';
import { BRAND } from '@/theme/theme';

const PIPELINE_STEPS = ['INGESTION', 'EXTRACTION', 'GRAPH_BUILDING', 'AWAITING_APPROVAL', 'ARCHITECTURE_DESIGN', 'CODE_GENERATION', 'VALIDATION', 'MIGRATION_PLANNING', 'COMPLETED'];
const STEP_LABELS = ['Ingest', 'Extract Rules', 'Build Graph', 'Approval', 'Architecture', 'Code Gen', 'Validate', 'Migration Plan', 'Done'];

function TabPanel({ children, value, index }: { children: React.ReactNode; value: number; index: number }) {
  return value === index ? <Box sx={{ py: 3 }}>{children}</Box> : null;
}

export default function ProjectDetailPage() {
  const router = useRouter();
  const { id } = router.query;
  const projectId = id as string;

  const [tab, setTab] = useState(0);
  const [project, setProject] = useState<ModernizationProject | null>(null);
  const [files, setFiles] = useState<AlchemistSourceFile[]>([]);
  const [pipelineStatus, setPipelineStatus] = useState<any>(null);
  const [rules, setRules] = useState<ExtractedRule[]>([]);
  const [graph, setGraph] = useState<any>(null);
  const [artifacts, setArtifacts] = useState<GeneratedArtifact[]>([]);
  const [audit, setAudit] = useState<AuditEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  const load = useCallback(async () => {
    if (!projectId) return;
    try {
      setLoading(true);
      const [proj, f, ps, r, g, art, aud] = await Promise.allSettled([
        fetchProject(projectId),
        fetchFiles(projectId),
        fetchPipelineStatus(projectId),
        fetchRules(projectId),
        fetchGraph(projectId),
        fetchArtifacts(projectId),
        fetchAuditEntries(projectId),
      ]);
      if (proj.status === 'fulfilled') setProject(proj.value);
      if (f.status === 'fulfilled') setFiles(f.value || []);
      if (ps.status === 'fulfilled') setPipelineStatus(ps.value);
      if (r.status === 'fulfilled') setRules(r.value || []);
      if (g.status === 'fulfilled') setGraph(g.value);
      if (art.status === 'fulfilled') setArtifacts(art.value || []);
      if (aud.status === 'fulfilled') setAudit(aud.value || []);
      setError(null);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => { load(); }, [load]);

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files?.length) return;
    setActionLoading(true);
    try {
      await uploadFiles(projectId, Array.from(e.target.files));
      load();
    } catch (err: any) { setError(err.message); }
    finally { setActionLoading(false); }
  };

  const handleStart = async () => {
    setActionLoading(true);
    try { await startPipeline(projectId); load(); }
    catch (err: any) { setError(err.message); }
    finally { setActionLoading(false); }
  };

  const handleResume = async () => {
    setActionLoading(true);
    try { await resumePipeline(projectId); load(); }
    catch (err: any) { setError(err.message); }
    finally { setActionLoading(false); }
  };

  const handleApprove = async (ruleId: string) => {
    try { await approveRule(projectId, ruleId); load(); }
    catch (err: any) { setError(err.message); }
  };

  const handleReject = async (ruleId: string) => {
    try { await rejectRule(projectId, ruleId); load(); }
    catch (err: any) { setError(err.message); }
  };

  const handleApproveAll = async () => {
    setActionLoading(true);
    try { await approveAllRules(projectId); load(); }
    catch (err: any) { setError(err.message); }
    finally { setActionLoading(false); }
  };

  const activeStep = pipelineStatus?.currentStep ? PIPELINE_STEPS.indexOf(pipelineStatus.currentStep) : -1;

  if (loading) return (
    <Box sx={{ minHeight: '60vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <CircularProgress sx={{ color: BRAND.accent }} />
    </Box>
  );

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Button startIcon={<ArrowBack />} onClick={() => router.push('/projects')} sx={{ color: 'grey.400', mb: 2 }}>Back to Projects</Button>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
        <AutoFixHigh sx={{ fontSize: 32, color: BRAND.accent }} />
        <Typography variant="h4" sx={{ color: '#fff', fontWeight: 700 }}>{project?.projectName}</Typography>
        {project?.status && <Chip label={project.status} size="small" color="info" />}
      </Box>
      {project?.description && <Typography variant="body2" color="grey.400" sx={{ mb: 3 }}>{project.description}</Typography>}

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      {/* Pipeline Stepper */}
      <Paper sx={{ bgcolor: BRAND.cardBg, p: 3, mb: 3, border: `1px solid ${BRAND.cardBorder}` }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" sx={{ color: '#fff' }}>Pipeline Progress</Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            {(!pipelineStatus || pipelineStatus.status === 'NO_RUNS') && (
              <Button variant="contained" startIcon={<PlayArrow />} onClick={handleStart} disabled={actionLoading || files.length === 0}
                sx={{ bgcolor: BRAND.accent, color: '#000' }}>Start Pipeline</Button>
            )}
            {pipelineStatus?.status === 'PAUSED' && (
              <Button variant="contained" startIcon={<PlayArrow />} onClick={handleResume} disabled={actionLoading}
                sx={{ bgcolor: '#7c4dff', color: '#fff' }}>Resume (After Approval)</Button>
            )}
            {pipelineStatus?.tokensUsed > 0 && (
              <Chip label={`${pipelineStatus.tokensUsed.toLocaleString()} tokens`} size="small" sx={{ bgcolor: '#1a1a3e', color: BRAND.accent }} />
            )}
          </Box>
        </Box>
        <Stepper activeStep={activeStep >= 0 ? activeStep : 0} alternativeLabel sx={{
          '& .MuiStepLabel-label': { color: 'grey.500', fontSize: '0.75rem' },
          '& .MuiStepLabel-label.Mui-active': { color: BRAND.accent },
          '& .MuiStepLabel-label.Mui-completed': { color: '#66bb6a' },
        }}>
          {STEP_LABELS.map((label) => (
            <Step key={label}><StepLabel>{label}</StepLabel></Step>
          ))}
        </Stepper>
      </Paper>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{
        mb: 2,
        '& .MuiTab-root': { color: 'grey.500' },
        '& .Mui-selected': { color: BRAND.accent },
        '& .MuiTabs-indicator': { bgcolor: BRAND.accent },
      }}>
        <Tab label={`Files (${files.length})`} />
        <Tab label={`Rules (${rules.length})`} />
        <Tab label="Graph" />
        <Tab label={`Artifacts (${artifacts.length})`} />
        <Tab label={`Audit (${audit.length})`} />
      </Tabs>

      {/* Tab 0: Files */}
      <TabPanel value={tab} index={0}>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <Button variant="outlined" component="label" startIcon={<Upload />} sx={{ borderColor: BRAND.accent, color: BRAND.accent }}>
            Upload Files
            <input type="file" hidden multiple onChange={handleUpload} />
          </Button>
        </Box>
        {files.length === 0 ? (
          <Typography color="grey.500" textAlign="center" py={4}>No source files uploaded yet. Upload legacy code files to begin.</Typography>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ color: 'grey.400' }}>Filename</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Language</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Lines</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Ingested</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {files.map((f) => (
                <TableRow key={f.id} sx={{ '&:hover': { bgcolor: '#111' } }}>
                  <TableCell sx={{ color: '#fff' }}>{f.filename}</TableCell>
                  <TableCell><Chip label={f.language} size="small" sx={{ bgcolor: '#1a1a3e', color: '#ce93d8' }} /></TableCell>
                  <TableCell sx={{ color: 'grey.400' }}>{f.lineCount}</TableCell>
                  <TableCell sx={{ color: 'grey.500', fontSize: '0.8rem' }}>{f.ingestedAt ? new Date(f.ingestedAt).toLocaleString() : '-'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </TabPanel>

      {/* Tab 1: Rules */}
      <TabPanel value={tab} index={1}>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <Button variant="outlined" startIcon={<DoneAll />} onClick={handleApproveAll} disabled={actionLoading}
            sx={{ borderColor: '#66bb6a', color: '#66bb6a' }}>Approve All Pending</Button>
        </Box>
        {rules.length === 0 ? (
          <Typography color="grey.500" textAlign="center" py={4}>No rules extracted yet. Start the pipeline to extract business rules.</Typography>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ color: 'grey.400' }}>Rule Name</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Type</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Confidence</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Status</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rules.map((r) => (
                <TableRow key={r.id}>
                  <TableCell sx={{ color: '#fff' }}>{r.ruleName}</TableCell>
                  <TableCell><Chip label={r.ruleType} size="small" sx={{ bgcolor: '#1a1a3e', color: '#90caf9' }} /></TableCell>
                  <TableCell sx={{ color: 'grey.400' }}>{r.confidence ? `${(r.confidence * 100).toFixed(0)}%` : '-'}</TableCell>
                  <TableCell>
                    <Chip label={r.approvalStatus} size="small"
                      color={r.approvalStatus === 'APPROVED' ? 'success' : r.approvalStatus === 'REJECTED' ? 'error' : 'warning'} />
                  </TableCell>
                  <TableCell>
                    {r.approvalStatus === 'PENDING' && (
                      <>
                        <IconButton size="small" onClick={() => handleApprove(r.id)} sx={{ color: '#66bb6a' }}><CheckCircle /></IconButton>
                        <IconButton size="small" onClick={() => handleReject(r.id)} sx={{ color: '#ef5350' }}><Cancel /></IconButton>
                      </>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </TabPanel>

      {/* Tab 2: Graph */}
      <TabPanel value={tab} index={2}>
        {!graph || (!graph.nodes?.length) ? (
          <Typography color="grey.500" textAlign="center" py={4}>Knowledge graph not built yet.</Typography>
        ) : (
          <Box>
            <Typography variant="h6" sx={{ color: '#fff', mb: 2 }}>
              {graph.nodes?.length || 0} nodes, {graph.edges?.length || 0} edges
            </Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ color: 'grey.400' }}>Node</TableCell>
                  <TableCell sx={{ color: 'grey.400' }}>Type</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(graph.nodes || []).slice(0, 50).map((n: any) => (
                  <TableRow key={n.id}>
                    <TableCell sx={{ color: '#fff' }}>{n.name}</TableCell>
                    <TableCell><Chip label={n.nodeType} size="small" sx={{ bgcolor: '#1a1a3e', color: '#ce93d8' }} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        )}
      </TabPanel>

      {/* Tab 3: Artifacts */}
      <TabPanel value={tab} index={3}>
        {artifacts.length === 0 ? (
          <Typography color="grey.500" textAlign="center" py={4}>No artifacts generated yet.</Typography>
        ) : (
          <Box>
            {['ARCHITECTURE_DESIGN', 'JAVA_SOURCE', 'TEST_SOURCE', 'CONFIG', 'VALIDATION_REPORT', 'MIGRATION_PLAN'].map(type => {
              const group = artifacts.filter(a => a.artifactType === type);
              if (group.length === 0) return null;
              return (
                <Box key={type} sx={{ mb: 3 }}>
                  <Typography variant="subtitle1" sx={{ color: BRAND.accent, mb: 1 }}>{type.replace(/_/g, ' ')} ({group.length})</Typography>
                  {group.map(a => (
                    <Card key={a.id} sx={{ bgcolor: '#111', mb: 1, border: `1px solid ${BRAND.cardBorder}` }}>
                      <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="body2" sx={{ color: '#fff' }}>{a.filename}</Typography>
                          <Typography variant="caption" color="grey.600">{a.generationModel}</Typography>
                        </Box>
                        {a.content && (
                          <Box sx={{ mt: 1, maxHeight: 200, overflow: 'auto', bgcolor: '#0a0a0a', p: 1, borderRadius: 1, fontFamily: 'monospace', fontSize: '0.75rem', color: 'grey.400' }}>
                            <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{a.content.substring(0, 2000)}{a.content.length > 2000 ? '\n...(truncated)' : ''}</pre>
                          </Box>
                        )}
                      </CardContent>
                    </Card>
                  ))}
                </Box>
              );
            })}
          </Box>
        )}
      </TabPanel>

      {/* Tab 4: Audit */}
      <TabPanel value={tab} index={4}>
        {audit.length === 0 ? (
          <Typography color="grey.500" textAlign="center" py={4}>No audit entries yet.</Typography>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ color: 'grey.400' }}>Time</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Action</TableCell>
                <TableCell sx={{ color: 'grey.400' }}>Actor</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {audit.map((e) => (
                <TableRow key={e.id}>
                  <TableCell sx={{ color: 'grey.500', fontSize: '0.8rem' }}>{new Date(e.timestamp).toLocaleString()}</TableCell>
                  <TableCell sx={{ color: '#fff' }}>{e.action}</TableCell>
                  <TableCell sx={{ color: 'grey.400' }}>{e.actor}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </TabPanel>
    </Container>
  );
}
