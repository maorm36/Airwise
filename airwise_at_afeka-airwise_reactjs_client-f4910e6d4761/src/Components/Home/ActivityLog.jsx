import React, { useMemo } from 'react';
import { Paper, Typography, Divider, Box, Stack, Chip } from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import WarningIcon from '@mui/icons-material/Warning';
import ErrorIcon from '@mui/icons-material/Error';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PropTypes from 'prop-types';

const DEFAULT_TARGET_ROWS = 5;
const NOTIFICATION_TYPES = {
  INFO: 'info',
  WARNING: 'warning',
  ERROR: 'error',
  SUCCESS: 'success',
};

const TYPE_CONFIG = {
  [NOTIFICATION_TYPES.INFO]: {
    color: '#1976d2',
    icon: InfoIcon,
    chipColor: 'info',
  },
  [NOTIFICATION_TYPES.WARNING]: {
    color: '#ff9800',
    icon: WarningIcon,
    chipColor: 'warning',
  },
  [NOTIFICATION_TYPES.ERROR]: {
    color: '#d32f2f',
    icon: ErrorIcon,
    chipColor: 'error',
  },
  [NOTIFICATION_TYPES.SUCCESS]: {
    color: '#2e7d32',
    icon: CheckCircleIcon,
    chipColor: 'success',
  },
};

function NotificationItem({ notification }) {
  if (notification.isEmpty) {
    return (
      <Box
        sx={{
          minHeight: 48,
          opacity: 0.1,
          borderLeft: '4px solid transparent',
          pl: 2,
        }}
      />
    );
  }

  // check if the notification alias has sub string "alert-"
  const isAlert = notification.alias && notification.alias.includes('alert-');
  const typeConfig = isAlert ? TYPE_CONFIG['warning'] : TYPE_CONFIG[notification.status] || TYPE_CONFIG[NOTIFICATION_TYPES.INFO];
  const Icon = typeConfig.icon;

  return (
    <Box
      sx={{
        borderLeft: `4px solid ${typeConfig.color}`,
        pl: 2,
        minHeight: 48,
        display: 'flex',
        flexDirection: 'column',
        gap: 0.5,
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <Icon sx={{ fontSize: 16, color: typeConfig.color }} />
        <Typography variant="caption" color="text.secondary">
          {notification.date} at {notification.time}
        </Typography>
        {notification.type && (
          <Chip
            label={notification.type}
            size="small"
            color={typeConfig.chipColor}
            sx={{ height: 20, fontSize: '0.7rem' }}
          />
        )}
      </Box>
      <Typography variant="body2" sx={{ pl: 3 }}>
        {notification.text}
      </Typography>
    </Box>
  );
}

NotificationItem.propTypes = {
  notification: PropTypes.shape({
    date: PropTypes.string,
    time: PropTypes.string,
    text: PropTypes.string,
    type: PropTypes.oneOf(Object.values(NOTIFICATION_TYPES)),
    isEmpty: PropTypes.bool,
  }).isRequired,
};

export default function ActivityLog({ 
  notifications = [], 
  fillEmptyRows = false, 
  targetRows = DEFAULT_TARGET_ROWS 
}) {
  // Memoize padded notifications to avoid recalculation
  const paddedNotifications = useMemo(() => {
    if (!fillEmptyRows || notifications.length >= targetRows) {
      return notifications;
    }

    const padded = [...notifications];
    const missing = targetRows - notifications.length;
    
    for (let i = 0; i < missing; i++) {
      padded.push({
        id: `empty-${i}`,
        date: '',
        time: '',
        text: '',
        isEmpty: true,
      });
    }
    
    return padded;
  }, [notifications, fillEmptyRows, targetRows]);

  const hasNotifications = notifications.length > 0;

  return (
    <Paper
      sx={{
        p: 2,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Typography variant="subtitle1" gutterBottom>
        Recent Notifications
      </Typography>

      <Divider sx={{ mb: 2 }} />

      <Stack
        spacing={2}
        sx={{
          flexGrow: 1,
          overflowY: 'auto',
          overflowX: 'hidden',
          // Custom scrollbar styling
          '&::-webkit-scrollbar': {
            width: '8px',
          },
          '&::-webkit-scrollbar-track': {
            backgroundColor: 'transparent',
          },
          '&::-webkit-scrollbar-thumb': {
            backgroundColor: 'rgba(0,0,0,0.2)',
            borderRadius: '4px',
          },
        }}
      >
        {!hasNotifications && !fillEmptyRows ? (
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              opacity: 0.5,
            }}
          >
            <Typography variant="body2" color="text.secondary">
              No notifications
            </Typography>
          </Box>
        ) : (
          paddedNotifications.map((notification, index) => (
            <NotificationItem
              key={notification.id || index}
              notification={notification}
            />
          ))
        )}
      </Stack>
    </Paper>
  );
}

ActivityLog.propTypes = {
  notifications: PropTypes.arrayOf(
    PropTypes.shape({
      date: PropTypes.string,
      time: PropTypes.string,
      text: PropTypes.string,
      type: PropTypes.oneOf(Object.values(NOTIFICATION_TYPES)),
    })
  ),
  fillEmptyRows: PropTypes.bool,
  targetRows: PropTypes.number,
};