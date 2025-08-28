import React, { useState } from 'react';
import { Box, TextField, Button } from '@mui/material';

export default function AddRoomForm({ onSubmit, siteId }) {
  const [roomName, setRoomName] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ name: roomName, siteId }); 
    setRoomName('');
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <TextField
        label="Room Name"
        fullWidth
        required
        value={roomName}
        onChange={(e) => setRoomName(e.target.value)}
      />
      <Button type="submit" variant="contained">Save</Button>
    </Box>
  );
}
