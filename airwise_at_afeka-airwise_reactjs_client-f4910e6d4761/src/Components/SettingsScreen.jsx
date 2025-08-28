import React, { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  Typography,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  InputAdornment,
  Button,
  CircularProgress
} from '@mui/material';
import WebApi from "../WebApi/WebApi";


export default function SettingsScreen() {
  const [vatRate, setVatRate] = useState('');
  const [currency, setCurrency] = useState('');
  const [costPerKwh, setCostPerKwh] = useState('');
  const [loading, setLoading] = useState(true); 

  const systemID = import.meta.env.VITE_SYSTEM_ID;
  const user = JSON.parse(localStorage.getItem('user'));
  const settings = JSON.parse(localStorage.getItem('settings'));
  const operator = JSON.parse(localStorage.getItem('operator'));
  const tenant = JSON.parse(localStorage.getItem('tenant'))
  const email = user?.userId?.email;

  useEffect(() => {
    async function loadSettings() {
        setVatRate(settings.objectDetails.vatRate);
        setCurrency(settings.objectDetails.currency);
        setCostPerKwh(settings.objectDetails.costPerKwh);
        setLoading(false); // done loading
      }
    

    if (systemID && email) {
      loadSettings();
    }
  }, []);

  const handleApply = async () => 
    {
    try {
      const updatedSettings = {
            vatRate: vatRate,
            currency: currency,
            costPerKwh: costPerKwh,
        }

      await WebApi.updateSettings(systemID, settings.id.objectId, updatedSettings,operator.userId.systemID,operator.userId.email,tenant.id.objectId);
     const updatedSettingsObj = await WebApi.getMySettings({ userSystemID: systemID, userEmail: user.userId.email, tenantId: tenant.id.objectId });
          if (!updatedSettingsObj) {
             throw new Error('Settings not found');
           }
      localStorage.setItem('settings', JSON.stringify(updatedSettingsObj));
      alert('Settings saved successfully.');
    } catch (error) {
      console.error('Failed to update settings:', error);
      alert('Failed to save settings.');
    }
  };

  if (loading) {
    return (
      <Box sx={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 3,
      }}
    >
      <Box sx={{ width: '100%', maxWidth: 500 }}>
        <Typography variant="h6" fontWeight="bold" mb={3}>
          Settings
        </Typography>

        <TextField
          label="VAT Rate"
          type="number"
          value={vatRate}
          onChange={(e) => setVatRate(e.target.value)}
          fullWidth
          margin="normal"
          InputProps={{
            endAdornment: <InputAdornment position="end">%</InputAdornment>,
          }}
          inputProps={{ min: 0 }}
        />

        <FormControl fullWidth margin="normal">
          <InputLabel>Currency</InputLabel>
          <Select
            value={currency}
            onChange={(e) => setCurrency(e.target.value)}
            label="Currency"
          >
            <MenuItem value="EUR">EUR</MenuItem>
            <MenuItem value="USD">USD</MenuItem>
            <MenuItem value="ILS">ILS</MenuItem>
          </Select>
        </FormControl>

        <TextField
          label="Cost per kWh"
          type="number"
          value={costPerKwh}
          onChange={(e) => setCostPerKwh(e.target.value)}
          fullWidth
          margin="normal"
          InputProps={{
            endAdornment: <InputAdornment position="end">{currency}</InputAdornment>,
          }}
          inputProps={{ min: 0 }}
        />

        <Box mt={3}>
          <Button variant="contained" onClick={handleApply} fullWidth>
            Apply
          </Button>
        </Box>
      </Box>
    </Box>
  );
}
