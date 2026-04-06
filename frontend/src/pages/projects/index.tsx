import React, { useEffect, useState } from 'react';
import {
  Container, Box, Typography, Grid, Card, CardContent, CardActions,
  Button, Chip, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, IconButton, CircularProgress, Alert,
} from '@mui/material';
import { Add, Delete, AutoFixHigh } from '@mui/icons-material';
import { useRouter } from 'next/router';
import { fetchProjects, createProject, deleteProject } from '@/services/alchemistService';
import type { ModernizationProject } from '@/types/alchemist';
import { BRAND } from '@/theme/theme';

const STATUS_COLORS: Record<string, 'default' | 'info' | 'warning' | 'success' | 'error'> = {
  CREATED: 'default',
  INGESTING: 'info',
  EXTRACTING: 'info',
  GRAPH_BUILDING: 'info',
  AWAITING_APPROVAL: 'warning',
  DESIGNING: 'info',
  GENERATING: 'info',
  VALIDATING: 'info',
  PLANNING_MIGRATION: 'info',
  COMPLETED: 'success',
  FAILED: 'error',
};

export default function ProjectsPage() {
  const router = useRouter();
  const [projects, setProjects] = useState<ModernizationProject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState({ projectName: '', description: '', sourceLanguage: 'COBOL', targetStack: 'Spring Boot 3 + Java 21' });

  const load = async () => {
    try {
      setLoading(true);
      const data = await fetchProjects();
      setProjects(Array.isArray(data) ? data : []);
      setError(null);
    } catch (e: any) {
      setError(e.message || 'Failed to load projects');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async () => {
    try {
      await createProject(form);
      setDialogOpen(false);
      setForm({ projectName: '', description: '', sourceLanguage: 'COBOL', targetStack: 'Spring Boot 3 + Java 21' });
      load();
    } catch (e: any) {
      setError(e.message);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteProject(id);
      load();
    } catch (e: any) {
      setError(e.message);
    }
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <AutoFixHigh sx={{ fontSize: 36, color: BRAND.accent }} />
          <Typography variant="h4" sx={{ color: '#fff', fontWeight: 700 }}>Projects</Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />} onClick={() => setDialogOpen(true)}
          sx={{ bgcolor: BRAND.accent, color: '#000', '&:hover': { bgcolor: BRAND.accentHover } }}>
          New Project
        </Button>
      </Box>

      <Typography variant="body1" sx={{ color: 'grey.400', mb: 4 }}>
        AI-powered legacy-to-cloud code modernization. Upload legacy source code, extract business rules, generate modern microservice architectures.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>{error}</Alert>}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress sx={{ color: BRAND.accent }} /></Box>
      ) : projects.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <AutoFixHigh sx={{ fontSize: 64, color: 'grey.700', mb: 2 }} />
          <Typography variant="h6" color="grey.500">No modernization projects yet</Typography>
          <Typography variant="body2" color="grey.600" sx={{ mb: 3 }}>Create your first project to start modernizing legacy code</Typography>
          <Button variant="outlined" onClick={() => setDialogOpen(true)} sx={{ borderColor: BRAND.accent, color: BRAND.accent }}>Create Project</Button>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {projects.map((p) => (
            <Grid item xs={12} sm={6} md={4} key={p.id}>
              <Card sx={{ bgcolor: BRAND.cardBg, border: `1px solid ${BRAND.cardBorder}`, cursor: 'pointer', '&:hover': { borderColor: BRAND.accent, transform: 'translateY(-2px)' }, transition: 'all 0.2s' }}
                onClick={() => router.push(`/projects/${p.id}`)}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                    <Typography variant="h6" sx={{ color: '#fff', fontWeight: 600 }}>{p.projectName}</Typography>
                    <Chip label={p.status} size="small" color={STATUS_COLORS[p.status] || 'default'} />
                  </Box>
                  {p.description && <Typography variant="body2" color="grey.400" sx={{ mb: 2 }}>{p.description}</Typography>}
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Chip label={p.sourceLanguage} size="small" variant="outlined" sx={{ borderColor: '#4a148c', color: '#ce93d8' }} />
                    <Chip label={p.targetStack} size="small" variant="outlined" sx={{ borderColor: '#1a237e', color: '#90caf9' }} />
                  </Box>
                </CardContent>
                <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
                  <Typography variant="caption" color="grey.600">
                    {p.createdAt ? new Date(p.createdAt).toLocaleDateString() : ''}
                  </Typography>
                  <IconButton size="small" onClick={(e) => { e.stopPropagation(); handleDelete(p.id); }} sx={{ color: 'grey.600', '&:hover': { color: 'error.main' } }}>
                    <Delete fontSize="small" />
                  </IconButton>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth
        PaperProps={{ sx: { bgcolor: BRAND.cardBg, color: '#fff' } }}>
        <DialogTitle>New Modernization Project</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '16px !important' }}>
          <TextField label="Project Name" value={form.projectName} onChange={(e) => setForm({ ...form, projectName: e.target.value })} fullWidth required
            InputLabelProps={{ sx: { color: 'grey.500' } }} InputProps={{ sx: { color: '#fff' } }} />
          <TextField label="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} fullWidth multiline rows={2}
            InputLabelProps={{ sx: { color: 'grey.500' } }} InputProps={{ sx: { color: '#fff' } }} />
          <TextField label="Source Language" value={form.sourceLanguage} onChange={(e) => setForm({ ...form, sourceLanguage: e.target.value })} select fullWidth
            InputLabelProps={{ sx: { color: 'grey.500' } }} InputProps={{ sx: { color: '#fff' } }}>
            {['COBOL', 'VB6', 'RPG', 'PL/I', 'FORTRAN', 'Other'].map(l => <MenuItem key={l} value={l}>{l}</MenuItem>)}
          </TextField>
          <TextField label="Target Stack" value={form.targetStack} onChange={(e) => setForm({ ...form, targetStack: e.target.value })} fullWidth
            InputLabelProps={{ sx: { color: 'grey.500' } }} InputProps={{ sx: { color: '#fff' } }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)} sx={{ color: 'grey.500' }}>Cancel</Button>
          <Button onClick={handleCreate} disabled={!form.projectName} sx={{ color: BRAND.accent }}>Create</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
}
