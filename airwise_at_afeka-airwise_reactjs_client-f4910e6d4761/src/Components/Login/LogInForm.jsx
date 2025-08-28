import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  CircularProgress
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import WebApi from '../../WebApi/WebApi';

export default function LogInForm() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const systemID = import.meta.env.VITE_SYSTEM_ID;

  const handleLogin = async (e) => {

    e.preventDefault();

    if (!email) {
      alert('Please enter an email');
      return;
    }

    setLoading(true);

    try {
      // Get operator from localStorage (should be set by App.js)
      const operator = JSON.parse(localStorage.getItem('operator'));
      if (!operator) {
        alert('System error: Operator not found. Please refresh the page.');
        setLoading(false);
        return;
      }

      // Try to login user
      let user;
      try {
        user = await WebApi.loginUser(systemID, email);
      } catch (error) {
        if (error.response && (error.response.status == 403 || error.response.status == 401)) {
          // User not found - redirect to register
          alert('No account found. Please register first.');
          navigate('/register', { state: { email } });
          return;
        }
        throw error;
      }

      // User exists, check tenant
      let tenant;
      try {
        tenant = await WebApi.getTenant({ 
          operatorSystemID: systemID, 
          operatorEmail: operator.userId.email, 
          tenantAlias: user.userId.email 
        });
      } catch (error) {
        console.error('Error fetching tenant:', error);
        tenant = null;
      }

      if (!tenant) {
        // User exists but no tenant - should not happen in normal flow
        // Redirect to register to create tenant
        alert('Account setup incomplete. Redirecting to registration.');
        navigate('/register', { state: { email } });
        return;
      }

      // Get settings
      let settings;
      try {
        settings = await WebApi.getMySettings({ 
          userSystemID: systemID,
          userEmail: email, 
          tenantId: tenant.id.objectId 
        });
      } catch (error) {
        console.error('Error fetching settings:', error);
        // If settings don't exist, we'll create them later or handle in the app
        settings = null;
      }

      // Save everything to localStorage
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('tenant', JSON.stringify(tenant));
      if (settings) {
        localStorage.setItem('settings', JSON.stringify(settings));
      }
      localStorage.setItem('loggedIn', 'true');
      
      navigate('/home');

    } catch (error) {
      console.error('Login failed:', error);
      alert('Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      onSubmit={handleLogin}
      component="form"
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        maxWidth: 400,
        mx: 'auto',
        mt: 6,
      }}
    >
      <Typography variant="h6" fontWeight="bold">
        Account Log In
      </Typography>

      <TextField
        label="Email"
        variant="outlined"
        fullWidth
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        disabled={loading}
        type="email"
        required
      />

      <Button 
        variant="contained" 
        type="submit"
        fullWidth
        disabled={loading}
      >
        {loading ? <CircularProgress size={24} /> : 'Login'}
      </Button>

      <Box sx={{ display: 'flex', justifyContent: 'flex-end', fontSize: 14 }}>
        <Typography
          variant="body2"
          color="primary"
          sx={{ cursor: 'pointer' }}
          onClick={() => navigate('/register')}
        >
          Don't have an account?
        </Typography>
      </Box>
    </Box>
  );
}