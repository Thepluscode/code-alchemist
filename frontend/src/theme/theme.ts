import { createTheme } from '@mui/material/styles';

export const BRAND = {
  pageBg: '#060914',
  cardBg: '#0d1225',
  cardBorder: '#1a1f3a',
  accent: '#00e5ff',
  accentHover: '#00b8d4',
  accentSoft: 'rgba(0, 229, 255, 0.08)',
  textPrimary: '#ffffff',
  textSecondary: '#9ca3af',
  textMuted: '#6b7280',
};

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: BRAND.accent },
    background: { default: BRAND.pageBg, paper: BRAND.cardBg },
    text: { primary: BRAND.textPrimary, secondary: BRAND.textSecondary },
  },
  typography: {
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "SF Pro Display", sans-serif',
    h4: { fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontWeight: 700, letterSpacing: '-0.015em' },
    h6: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: { borderRadius: 10 },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 8 },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          border: `1px solid ${BRAND.cardBorder}`,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
  },
});
