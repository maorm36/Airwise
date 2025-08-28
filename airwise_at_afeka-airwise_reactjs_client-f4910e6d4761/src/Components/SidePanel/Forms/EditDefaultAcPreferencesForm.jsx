import React, { useState } from 'react';
import {
  Box, TextField, Button, FormControl,
  InputLabel, Select, MenuItem
} from '@mui/material';
import { AcConstants } from '../../Utils/AcConstants';

export default function EditDefaultAcPreferencesForm({ onSubmit }) {
  const [temp, setTemp] = useState('');
  const [mode, setMode] = useState('Cooling');
  const [fanSpeed, setFanSpeed] = useState('Medium');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ temp, mode, fanSpeed });
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <TextField
        type='number'
        inputProps={{ min: 16, max: 30 }} // Assuming a range of 16 to 30 degrees
        label="Temperature"
        fullWidth
        required
        value={temp}
        onChange={(e) => setTemp(e.target.value)}
      />
      <FormControl fullWidth>
        <InputLabel>Mode</InputLabel>
        <Select value={mode} onChange={(e) => setMode(e.target.value)}>
          <MenuItem value={AcConstants.MODES.COOL}>Cooling</MenuItem>
          <MenuItem value={AcConstants.MODES.HEAT}>Heating</MenuItem>
        </Select>
      </FormControl>
      <FormControl fullWidth>
        <InputLabel>Fan Speed</InputLabel>
        <Select value={fanSpeed} onChange={(e) => setFanSpeed(e.target.value)}>
          <MenuItem value={AcConstants.FAN_SPEEDS.LOW}>Low</MenuItem>
          <MenuItem value={AcConstants.FAN_SPEEDS.MEDIUM}>Medium</MenuItem>
          <MenuItem value={AcConstants.FAN_SPEEDS.MEDIUM}>High</MenuItem>
        </Select>
      </FormControl>
      <Button type="submit" variant="contained">Save</Button>
    </Box>
  );
}
