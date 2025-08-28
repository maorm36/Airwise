import React from 'react';
import { Box } from '@mui/material';
import LogInForm from './LogInForm';

export default function LogInPage() {
  return (
    <Box sx={{ p: 3, mt: 6 }}> {/*added mt: 6 for spacing from top */}
      <LogInForm />
    </Box>
  );
}
