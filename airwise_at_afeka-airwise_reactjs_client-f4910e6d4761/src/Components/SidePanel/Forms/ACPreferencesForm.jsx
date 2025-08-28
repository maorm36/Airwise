import React, { useState } from 'react';
import {
  Box, TextField, Button, FormControl,
  InputLabel, Select, MenuItem, Checkbox, FormControlLabel,
  InputAdornment
} from '@mui/material';
import { AcConstants } from '../../Utils/AcConstants';

export default function ACPreferencesForm({ type, entity, onSubmit }) {
  const [name, setName] = useState(entity.alias || '');
  const [temperature, setTemperature] = useState(entity.objectDetails.temperature || 22);
  const [mode, setMode] = useState(entity.objectDetails.mode || AcConstants.MODES.COOL);
  const [fanSpeed, setFanSpeed] = useState(entity.objectDetails.fanSpeed || AcConstants.FAN_SPEEDS.MEDIUM);

  const handleSubmit = (e) => {
    e.preventDefault();
    let tempr = Number(temperature); // Ensure temperature is a number
    if (isNaN(tempr) || tempr < 16 || tempr > 30) {
      alert('Temperature must be a number between 16 and 30');
      return;
    }
    onSubmit({ name, temperature: tempr, mode, fanSpeed });
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>

      { type === 'RoomPreferences' && (
        <TextField
          label="Name"
          type="text"
          fullWidth
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
          InputProps={{
            endAdornment: <InputAdornment position="end">{name}</InputAdornment>,
          }}      />
      )}

      <TextField
        label="Temperature"
        type="number"
        fullWidth
        required
        value={temperature}
        onChange={(e) => setTemperature(e.target.value)}
        InputProps={{
          endAdornment: <InputAdornment position="end">{temperature}&deg;</InputAdornment>,
        }}
        inputProps={{ min: 16, max: 30, step: 1 }}


      />

      <FormControl fullWidth margin="normal">
        <InputLabel>Mode</InputLabel>
        <Select value={mode} label="Mode" onChange={(e) => setMode(e.target.value)}>
          <MenuItem value={AcConstants.MODES.COOL}>Cooling</MenuItem>
          <MenuItem value={AcConstants.MODES.HEAT}>Heating</MenuItem>
        </Select>
      </FormControl>
      <FormControl fullWidth>
        <InputLabel>Fan Speed</InputLabel>
        <Select value={fanSpeed} label="Fan speed" onChange={(e) => setFanSpeed(e.target.value)}>
          <MenuItem value={AcConstants.FAN_SPEEDS.LOW}>Low</MenuItem>
          <MenuItem value={AcConstants.FAN_SPEEDS.MEDIUM}>Medium</MenuItem>
          <MenuItem value={AcConstants.FAN_SPEEDS.HIGH}>High</MenuItem>
        </Select>
      </FormControl>
      <Button type="submit" variant="contained">Save</Button>
    </Box >
  );
}
