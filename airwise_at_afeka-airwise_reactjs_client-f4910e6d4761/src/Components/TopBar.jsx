import React from 'react';
import { AppBar, Box, Toolbar, Button, Typography, IconButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';

export default function TopBar({ toggleMode, darkMode }) {
  const navigate = useNavigate();
  const user = localStorage.getItem('user');

  const handleLogout = () => {
    localStorage.removeItem('user');
    localStorage.removeItem('tenant');
    localStorage.removeItem('settings');
    localStorage.removeItem('loggedIn');
    navigate('/login');
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static" sx={{ backgroundColor: 'black', color: 'white' }}>
        <Toolbar>
          {user ? (
            <>
              <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
                <Button onClick={() => navigate('/home')} sx={{ my: 2, color: 'white' }}>
                  Home
                </Button>
                <Button onClick={() => navigate('/ac')} sx={{ my: 2, color: 'white' }}>
                  AC Control
                </Button>
                {/* <Button onClick={() => navigate('/notifications')} sx={{ my: 2, color: 'white' }}>
                  Notifications
                </Button> */}
                <Button onClick={() => navigate('/settings')} sx={{ my: 2, color: 'white' }}>
                  Settings
                </Button>
              </Box>

              <IconButton color="inherit" onClick={toggleMode} sx={{ mr: 2 }}>
                {darkMode ? <LightModeIcon /> : <DarkModeIcon />}
              </IconButton>

              <Button color="inherit" onClick={handleLogout}>
                Log out
              </Button>
            </>
          ) : (
            <Typography variant="h6" sx={{ flexGrow: 1 }}>
              Airwise
            </Typography>
          )}
        </Toolbar>
      </AppBar>
    </Box>
  );
}
