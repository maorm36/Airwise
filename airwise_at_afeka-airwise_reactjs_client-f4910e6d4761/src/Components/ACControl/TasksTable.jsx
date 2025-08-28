import React from 'react';
import { Paper, Stack, Typography, Button, Box, IconButton, Chip } from '@mui/material';
import { DataGrid, GridDeleteIcon } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import { ScheduleConstants } from '../Utils/ScheduleConstants';

export default function TasksTable({ rows, selectedAC, onAdd, onDelete }) {
  const columns = [
    {
      field: 'taskName',
      headerName: 'Task Name',
      flex: 1.5,
      renderCell: (params) => params.row.objectDetails.taskName || 'Unnamed Task'
    },
    {
      field: 'action',
      headerName: 'Action',
      flex: 1,
      renderCell: (params) => {
        const action = params.row.objectDetails?.action || 'Unknown';
        const color = action === ScheduleConstants.ACTIONS.TURN_ON ? 'success' : action === ScheduleConstants.ACTIONS.TURN_OFF ? 'error' : 'primary';
        return <Chip label={action} size="small" color={color} />;
      }
    },
    {
      field: 'startTime',
      headerName: 'Start Time',
      flex: 1,
      renderCell: (params) => params.row.objectDetails?.startTime || '-'
    },
    {
      field: 'endTime',
      headerName: 'End Time',
      flex: 1,
      renderCell: (params) => params.row.objectDetails?.endTime || '-'
    },
    {
      field: 'repeat',
      headerName: 'Frequency',
      flex: 1,
      renderCell: (params) => params.row.objectDetails?.repeat || 'Once'
    },
    {
      field: 'preferences',
      headerName: 'Preferences',
      flex: 2,
      renderCell: (params) => {
        const details = params.row.objectDetails;
        if (!details || params.row.objectDetails?.action === ScheduleConstants.ACTIONS.TURN_OFF) return '-';

        if (details.useCurrentPreferences === true) {
          return 'Use Current AC Settings';
        }

        return `${details.temperature || '-'}°, ${details.mode || '-'}, ${details.fanSpeed || '-'}`;
      }
    },
    {
      field: 'status',
      headerName: 'Status',
      flex: 1,
      renderCell: (params) => {
        const status = params.row.status || ScheduleConstants.STATUS.INACTIVE;
      
        return (
          // Render a Chip based on the status - executed, inactive, scheduled, or active
          <Chip
            label={status.charAt(0).toUpperCase() + status.slice(1)}
            size="small"
            color={
              status === ScheduleConstants.STATUS.ACTIVE ? 'success' :
              status === ScheduleConstants.STATUS.INACTIVE ? 'default' :
              status === ScheduleConstants.STATUS.SCHEDULED ? 'primary' :
              'warning'
            }
            variant={status === ScheduleConstants.STATUS.INACTIVE ? 'outlined' : 'filled'}
          />
        );
      }
    },
    {
      field: 'controls',
      headerName: 'Controls',
      flex: 1.5,
      renderCell: (params) => {
        if (!params || !params.row) return null;
        const isActive = params.row.status === ScheduleConstants.STATUS.ACTIVE;

        return (
          <Stack direction="row" spacing={0.5}>
            <IconButton
              size="small"
              color="error"
              onClick={(e) => {
                e.stopPropagation();
                if (window.confirm(`Delete task "${params.row.alias}"?`)) {
                  onDelete && onDelete(params.row);
                }
              }}
              title="Delete Task"
            >
              <GridDeleteIcon fontSize="small" />
            </IconButton>
          </Stack>
        );
      }
    }
  ];

  return (
    <Paper sx={{ p: 2 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1}>
        <Typography variant="subtitle1">
          Scheduled Tasks {rows?.length ? `(${rows.length})` : '(0)'}
        </Typography>
        <Button variant="contained" size="small" startIcon={<AddIcon />} onClick={onAdd}>
          Create Schedule
        </Button>
      </Stack>
      <Box sx={{ minHeight: 300, width: '100%' }}>
        {!rows || rows.length === 0 ? (
          <Box sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: 200,
            backgroundColor: 'background.default',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 1
          }}>
            <Typography variant="body2" color="text.secondary">
               { selectedAC ? 'No scheduled tasks for this AC'  : 'Please select an AC to view tasks' }
            </Typography>
          </Box>
        ) : (
          <DataGrid
            getRowId={row => row.id?.objectId || row.objectId || `task-${Math.random()}`}
            rows={rows}
            columns={columns}
            autoHeight
            hideFooter
            disableRowSelectionOnClick
          />
        )}
      </Box>
    </Paper>
  );
}