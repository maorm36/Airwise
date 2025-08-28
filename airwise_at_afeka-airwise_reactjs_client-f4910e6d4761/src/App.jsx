import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Router from './Components/Router';
import TopBar from './Components/TopBar';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import WebApi from './WebApi/WebApi';

const systemID = import.meta.env.VITE_SYSTEM_ID;
const operatorEmail = import.meta.env.VITE_OPERATOR_EMAIL;

export default function App() {
  const navigate = useNavigate();
  const [isInitialized, setIsInitialized] = useState(false);

  // Ensure operator exists before everything else
  useEffect(() => {
    async function initializeOperator() {
      try {
        // Check if operator exists in localStorage
        let operator = JSON.parse(localStorage.getItem('operator'));
        
        if (!operator) {
          // Try to get operator from backend
          try {
            operator = await WebApi.loginUser(systemID, operatorEmail);
          } catch (error) {
            // If operator doesn't exist, create it
            if (error.response && (error.response.status == 403 || error.response.status == 401)) {
              operator = await WebApi.createUser({
                email: operatorEmail,
                role: 'OPERATOR',
                username: 'SystemOperator',
                avatar: 'SystemOperator'
              });
            } else {
              throw error;
            }
          }
          
          if (operator) {
            localStorage.setItem('operator', JSON.stringify(operator));
          } else {
            console.error('Failed to initialize operator');
            alert('System error: Could not initialize operator');
            return;
          }
        }

        // Check user and tenant consistency
        const user = JSON.parse(localStorage.getItem('user'));
        const loggedIn = JSON.parse(localStorage.getItem('loggedIn'));
        
        if (loggedIn && user && operator) {
          try {
            const tenant = await WebApi.getTenant({ 
              operatorSystemID: systemID, 
              operatorEmail: operator.userId.email, 
              tenantAlias: user.userId.email 
            });
            
            if (!tenant) {
              // User exists but no tenant - inconsistent state
              localStorage.removeItem('tenant');
              localStorage.removeItem('user');
              localStorage.removeItem('loggedIn');
              localStorage.removeItem('settings');
              navigate('/');
            }
          } catch (error) {
            console.error('Error checking tenant:', error);
            localStorage.removeItem('tenant');
            localStorage.removeItem('user');
            localStorage.removeItem('loggedIn');
            localStorage.removeItem('settings');
            navigate('/');
          }
        } else if (loggedIn && !user) {
          // Logged in but no user - clear everything
          localStorage.removeItem('loggedIn');
          localStorage.removeItem('tenant');
          localStorage.removeItem('settings');
          navigate('/');
        }

        setIsInitialized(true);
      } catch (error) {
        console.error('Error initializing app:', error);
        alert('System initialization failed. Please refresh the page.');
      }
    }

    initializeOperator();
  }, [navigate]);

  const [darkMode, setDarkMode] = useState(() => {
    const saved = localStorage.getItem('darkMode');
    return saved === 'true'; 
  });

  const toggleMode = () => {
    const newMode = !darkMode;
    setDarkMode(newMode);
    localStorage.setItem('darkMode', newMode); 
  };

  const theme = createTheme({
    palette: {
      mode: darkMode ? 'dark' : 'light',
    },
  });

  if (!isInitialized) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
          Loading...
        </div>
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <TopBar toggleMode={toggleMode} darkMode={darkMode} />
      <Router />
    </ThemeProvider>
  );
}