import React, { useState } from 'react';
import { Box, TextField, Button } from '@mui/material';

export default function AddAcForm({ onSubmit, roomId }) {
  const [sn, setSn] = useState('');
  const [manufacturer, setManufacturer] = useState('');
  const [wattsOfDevice, setWattsOfDevice] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ serialNumber: sn, manufacturer, roomId, wattsOfDevice });
    setSn('');
    setManufacturer('');
    setWattsOfDevice('');
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <TextField
        label="Serial Number"
        fullWidth
        required
        value={sn}
        onChange={(e) => setSn(e.target.value)}
      />
      <TextField
        label="Manufacturer"
        fullWidth
        required
        value={manufacturer}
        onChange={(e) => setManufacturer(e.target.value)}
      />
      <TextField
        label="Watts of Device"
        fullWidth
        required
        value={wattsOfDevice}
        onChange={(e) => setWattsOfDevice(e.target.value)}
      />
      <Button type="submit" variant="contained">Save</Button>
    </Box>
  );
}
