import { useEffect } from 'react';
import { useRouter } from 'next/router';
import { Box, CircularProgress } from '@mui/material';
import { BRAND } from '@/theme/theme';

export default function Home() {
  const router = useRouter();
  useEffect(() => {
    router.replace('/projects');
  }, [router]);
  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
      <CircularProgress sx={{ color: BRAND.accent }} />
    </Box>
  );
}
