import React, { useState } from 'react';
import { Box, TextField, Button } from '@mui/material';

export default function AddSiteForm({ onSubmit }) {
  const [siteName, setSiteName] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ name: siteName });
    setSiteName('');
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <TextField
        label="Site Name"
        fullWidth
        required
        value={siteName}
        onChange={(e) => setSiteName(e.target.value)}
      />
      <Button type="submit" variant="contained">Save</Button>
    </Box>
  );
}
