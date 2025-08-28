import React, { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Avatar,
  CircularProgress
} from '@mui/material';
import PhotoCamera from '@mui/icons-material/PhotoCamera';
import { useNavigate, useLocation } from 'react-router-dom';
import WebApi from '../../WebApi/WebApi';
import UserRole from './UserRole';

export default function RegisterForm() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [avatar, setAvatar] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const systemID = import.meta.env.VITE_SYSTEM_ID;

  // Pre-fill email if coming from login
  useEffect(() => {
    if (location.state?.email) {
      setEmail(location.state.email);
    }
  }, [location.state]);

  const handleRegister = async (e) => {

    e.preventDefault();

    if (!email || !name || !avatar) {
      alert('Please fill in all required fields');
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

      // Check if user already exists
      let user;
      let userExists = false;
      
      try {
      
        user = await WebApi.loginUser(systemID, email);
        userExists = true;
      
      } catch (error) {
        if (error.response && (error.response.status == 403 || error.response.status == 401)) {
          // User doesn't exist - this is expected for new registration
          userExists = false;
        } else {
          throw error;
        }
      }

      if (userExists) {
        // User already exists - check if they have a tenant
        let tenant;
        try {
          tenant = await WebApi.getTenant({ 
            operatorSystemID: systemID, 
            operatorEmail: operator.userId.email, 
            tenantAlias: user.userId.email 
          });
        } catch (error) {
          console.error('Error checking tenant:', error);
          tenant = null;
        }

        if (tenant) {
          // User and tenant already exist - redirect to login
          alert('Account already exists. Please login.');
          navigate('/');
          return;
        } else {
          // User exists but no tenant - create tenant and settings
          try {
            tenant = await WebApi.createTenant(
              operator.userId.systemID, 
              operator.userId.email, 
              user.userId.email
            );
            
            const settings = await WebApi.createSettings(
              operator.userId.systemID, 
              operator.userId.email, 
              tenant.id.objectId
            );

            // Save to localStorage and redirect to home
            localStorage.setItem('tenant', JSON.stringify(tenant));
            localStorage.setItem('user', JSON.stringify(user));
            if (settings) {
              localStorage.setItem('settings', JSON.stringify(settings));
            }
            localStorage.setItem('loggedIn', 'true');
            navigate('/home');
          } catch (error) {
            console.error('Error creating tenant/settings:', error);
            alert('Error setting up account. Please try again.');
          }
        }
      } else {
        // User doesn't exist - create new user, tenant, and settings
        try {
          const newUser = {
            email,
            role: UserRole.END_USER,
            username: name,
            avatar: avatar
          };

          const createdUser = await WebApi.createUser(newUser);
          
          const tenant = await WebApi.createTenant(
            operator.userId.systemID, 
            operator.userId.email, 
            createdUser.userId.email
          );
          
          const settings = await WebApi.createSettings(
            operator.userId.systemID, 
            operator.userId.email, 
            tenant.id.objectId
          );

          // Save to localStorage and redirect to home
          localStorage.setItem('tenant', JSON.stringify(tenant));
          localStorage.setItem('user', JSON.stringify(createdUser));
          if (settings) {
            localStorage.setItem('settings', JSON.stringify(settings));
          }
          
          localStorage.setItem('loggedIn', 'true');
          navigate('/home');

        } catch (error) {
          console.error('Error creating new user:', error);
          alert('Registration failed. Please try again.');
        }
      }
    } catch (error) {
      console.error('Registration error:', error);
      alert('An error occurred during registration. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      onSubmit={handleRegister}
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
        Register
      </Typography>

      <TextField
        label="UserName"
        variant="outlined"
        fullWidth
        value={name}
        onChange={(e) => setName(e.target.value)}
        disabled={loading}
        required
      />

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

      <TextField
        label="Avatar"
        variant="outlined"
        fullWidth
        value={avatar}
        onChange={(e) => setAvatar(e.target.value)}
        disabled={loading}
        type="text"
        required
      />

      <Button 
        variant="contained" 
        type="submit"
        fullWidth
        disabled={loading}
      >
        {loading ? <CircularProgress size={24} /> : 'Sign Up'}
      </Button>

      <Box sx={{ display: 'flex', justifyContent: 'center', fontSize: 14 }}>
        <Typography variant="body2" sx={{ mr: 1 }}>
          Already have an account?
        </Typography>
        <Typography
          variant="body2"
          color="primary"
          sx={{ cursor: 'pointer' }}
          onClick={() => navigate('/login')}
        >
          Login
        </Typography>
      </Box>
    </Box>
  );
}