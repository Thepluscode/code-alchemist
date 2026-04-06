import React from 'react';
import { AppBar, Toolbar, Box, Typography, Container } from '@mui/material';
import { AutoFixHigh } from '@mui/icons-material';
import { useRouter } from 'next/router';
import { BRAND } from '@/theme/theme';

export default function Layout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: BRAND.pageBg }}>
      <AppBar
        position="sticky"
        elevation={0}
        sx={{
          bgcolor: 'rgba(13, 18, 37, 0.8)',
          backdropFilter: 'blur(12px)',
          borderBottom: `1px solid ${BRAND.cardBorder}`,
        }}
      >
        <Container maxWidth="xl">
          <Toolbar disableGutters sx={{ minHeight: '64px !important' }}>
            <Box
              sx={{ display: 'flex', alignItems: 'center', gap: 1.5, cursor: 'pointer' }}
              onClick={() => router.push('/')}
            >
              <Box
                sx={{
                  width: 36,
                  height: 36,
                  borderRadius: 2,
                  background: `linear-gradient(135deg, ${BRAND.accent} 0%, #7c3aed 100%)`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <AutoFixHigh sx={{ color: '#000', fontSize: 22 }} />
              </Box>
              <Box>
                <Typography variant="h6" sx={{ color: '#fff', fontWeight: 700, lineHeight: 1.1 }}>
                  Code Alchemist
                </Typography>
                <Typography variant="caption" sx={{ color: BRAND.textMuted, fontSize: '0.7rem' }}>
                  Legacy → Cloud · AI Modernization
                </Typography>
              </Box>
            </Box>
          </Toolbar>
        </Container>
      </AppBar>
      {children}
    </Box>
  );
}
