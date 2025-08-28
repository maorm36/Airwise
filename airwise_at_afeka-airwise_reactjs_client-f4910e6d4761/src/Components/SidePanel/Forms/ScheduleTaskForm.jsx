import React, { useState, useEffect } from 'react';
import {
  Box, TextField, Button, FormControl,
  InputLabel, Select, MenuItem, RadioGroup,
  FormControlLabel, Radio, Typography, Alert
} from '@mui/material';
import { ScheduleConstants } from '../../Utils/ScheduleConstants';
import { AcConstants } from '../../Utils/AcConstants';

export default function ScheduleTaskForm({ onSubmit, selectedAC, initialData = {} }) {
  const [taskName, setTaskName] = useState(initialData.taskName || '');
  const [action, setAction] = useState(initialData.action || ScheduleConstants.ACTIONS.TURN_ON);
  const [startTime, setStartTime] = useState(initialData.startTime || '');
  const [endTime, setEndTime] = useState(initialData.endTime || '');
  const [repeat, setRepeat] = useState(initialData.repeat || ScheduleConstants.FREQUENCIES.ONCE);
  const [useCurrentPreferences, setUseCurrentPreferences] = useState(
    initialData.useCurrentPreferences !== undefined ? initialData.useCurrentPreferences : true
  );
  const [temp, setTemp] = useState(initialData.temp || 20);
  const [mode, setMode] = useState(initialData.mode || AcConstants.MODES.COOL);
  const [fanSpeed, setFanSpeed] = useState(initialData.fanSpeed || AcConstants.FAN_SPEEDS.MEDIUM);

  const isEditMode = !!initialData.taskName;

  // Initialize with selected AC's current preferences when form opens
  useEffect(() => {
    if (selectedAC && selectedAC.objectDetails) {
      setTemp(selectedAC.objectDetails.temperature || 22);
      setMode(selectedAC.objectDetails.mode || AcConstants.MODES.COOL);
      setFanSpeed(selectedAC.objectDetails.fanSpeed || AcConstants.FAN_SPEEDS.MEDIUM);
    }
  }, [selectedAC]);

  const handleSubmit = (e) => {
    e.preventDefault();

    const taskData = {
      taskName,
      action,
      startTime,
      endTime: endTime || undefined, // Only include if provided
      repeat,
      acId: selectedAC?.id?.objectId || selectedAC?.objectId,
      useCurrentPreferences: Boolean(useCurrentPreferences), // Ensure boolean
      ...(action !== ScheduleConstants.ACTIONS.TURN_OFF && {
        temp: useCurrentPreferences ? selectedAC?.objectDetails?.temperature : Number(temp),
        mode: useCurrentPreferences ? selectedAC?.objectDetails?.mode : mode,
        fanSpeed: useCurrentPreferences ? selectedAC?.objectDetails?.fanSpeed : fanSpeed,
      })
    };

    onSubmit(taskData);
  };

  // Disable preference fields based on action and preference mode
  const isPreferenceDisabled = action === ScheduleConstants.ACTIONS.TURN_OFF || useCurrentPreferences;

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2, p: 3 }}>
      {selectedAC && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Creating task for AC: <strong>{selectedAC.alias}</strong>
        </Alert>
      )}

      <TextField
        label="Task Name"
        fullWidth
        required
        value={taskName}
        onChange={(e) => setTaskName(e.target.value)}
        placeholder="e.g., Morning Cooldown, Night Mode"
      />

      <FormControl fullWidth>
        <InputLabel>Action</InputLabel>
        <Select label="Action" value={action} onChange={(e) => setAction(e.target.value)}>
          <MenuItem value={ScheduleConstants.ACTIONS.TURN_ON}>Turn On</MenuItem>
          <MenuItem value={ScheduleConstants.ACTIONS.TURN_OFF}>Turn Off</MenuItem>
        </Select>
      </FormControl>

      <TextField
        label="Start Time"
        type="time"
        fullWidth
        required
        InputLabelProps={{ shrink: true }}
        value={startTime}
        onChange={(e) => setStartTime(e.target.value)}
      />

      {action === ScheduleConstants.ACTIONS.TURN_ON && (
        <TextField
          label="End Time (Auto Turn Off)"
          type="time"
          fullWidth
          InputLabelProps={{ shrink: true }}
          value={endTime}
          onChange={(e) => setEndTime(e.target.value)}
          helperText="Optional: AC will turn off at this time"
        />
      )}

      <FormControl fullWidth>
        <InputLabel>Repeat</InputLabel>
        <Select label="Repeat" value={repeat} onChange={(e) => setRepeat(e.target.value)}>
          <MenuItem value={ScheduleConstants.FREQUENCIES.ONCE}>Once</MenuItem>
          <MenuItem value={ScheduleConstants.FREQUENCIES.EVERY_DAY}>Every Day</MenuItem>
          <MenuItem value={ScheduleConstants.FREQUENCIES.EVERY_WEEKDAY}>Every Weekday (Mon-Fri)</MenuItem>
          <MenuItem value={ScheduleConstants.FREQUENCIES.WEEKENDS}>Weekends (Sat-Sun)</MenuItem>
        </Select>
      </FormControl>

      {action !== ScheduleConstants.ACTIONS.TURN_OFF && (
        <>
          <Typography variant="subtitle2" sx={{ mt: 2 }}>AC Preferences:</Typography>
          <RadioGroup 
            value={useCurrentPreferences.toString()} 
            onChange={(e) => setUseCurrentPreferences(e.target.value === 'true')}
          >
            <FormControlLabel
              value="true"
              control={<Radio />}
              label={`Use Current AC Settings (${selectedAC?.objectDetails?.temperature}°, ${selectedAC?.objectDetails?.mode}, ${selectedAC?.objectDetails?.fanSpeed})`}
            />
            <FormControlLabel
              value="false"
              control={<Radio />}
              label="Set Custom Preferences"
            />
          </RadioGroup>

          <TextField
            label="Temperature"
            type="number"
            fullWidth
            value={temp}
            onChange={(e) => setTemp(e.target.value)}
            disabled={isPreferenceDisabled}
            inputProps={{ min: 16, max: 30 }}
            helperText={isPreferenceDisabled ? "Using current AC temperature" : ""}
          />

          <FormControl fullWidth disabled={isPreferenceDisabled}>
            <InputLabel>Mode</InputLabel>
            <Select label="Mode" value={mode} onChange={(e) => setMode(e.target.value)}>
              <MenuItem value={AcConstants.MODES.COOL}>Cooling</MenuItem>
              <MenuItem value={AcConstants.MODES.HEAT}>Heating</MenuItem>
            </Select>
          </FormControl>

          <FormControl fullWidth disabled={isPreferenceDisabled}>
            <InputLabel>Fan Speed</InputLabel>
            <Select label="Fan Speed" value={fanSpeed} onChange={(e) => setFanSpeed(e.target.value)}>
              <MenuItem value={AcConstants.FAN_SPEEDS.LOW}>Low</MenuItem>
              <MenuItem value={AcConstants.FAN_SPEEDS.MEDIUM}>Medium</MenuItem>
              <MenuItem value={AcConstants.FAN_SPEEDS.HIGH}>High</MenuItem>
            </Select>
          </FormControl>
        </>
      )}

      <Button type="submit" variant="contained" sx={{ mt: 2 }}>
        {isEditMode ? 'Update Task' : 'Create Task'}
      </Button>
    </Box>
  );
}