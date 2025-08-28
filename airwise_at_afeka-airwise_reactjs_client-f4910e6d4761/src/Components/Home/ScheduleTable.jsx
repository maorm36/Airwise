import React, { useMemo } from 'react';
import { Paper, Typography, Chip, Box } from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import PropTypes from 'prop-types';


const ACTION_COLORS = {
  'Turn On': 'success',
  'Turn Off': 'error',
  'Adjust': 'warning',
  'Auto': 'info',
};

const MODE_COLORS = {
  'Cool': 'info',
  'Heat': 'error',
  'Fan': 'default',
  'Auto': 'primary',
  'Dry': 'warning',
};

export default function ScheduleTable({ rows = [] }) {
  const columns = useMemo(() => [
    { 
      field: 'taskName', 
      headerName: 'Task Name', 
      flex: 1.5,
      minWidth: 150,
      renderCell: (params) => (
        <Typography variant="body2" sx={{ fontWeight: 500 }}>
          {params.value || 'Unnamed Task'}
        </Typography>
      ),
    },
    { 
      field: 'mode', 
      headerName: 'Mode', 
      flex: 0.8,
      minWidth: 80,
      renderCell: (params) => (
        <Chip
          label={params.value || 'Auto'}
          size="small"
          color={MODE_COLORS[params.value] || 'default'}
          sx={{ fontSize: '0.75rem' }}
        />
      ),
    },
    { 
      field: 'startTime', 
      headerName: 'Start Time', 
      flex: 1,
      minWidth: 100,
      renderCell: (params) => (
        <Typography variant="body2">
          {params.value ? params.value : 'N/A'}
        </Typography>
      ),
    },
    { 
      field: 'endTime', 
      headerName: 'End Time', 
      flex: 1,
      minWidth: 100,
      renderCell: (params) => (
        <Typography variant="body2">
          {params.value ? params.value : 'N/A'}
        </Typography>
      ),
    },
    { 
      field: 'frequency', 
      headerName: 'Frequency', 
      flex: 1,
      minWidth: 100,
      renderCell: (params) => (
        <Typography variant="body2" sx={{ fontStyle: params.value === 'Once' ? 'italic' : 'normal' }}>
          {params.value || 'Once'}
        </Typography>
      ),
    },
    { 
      field: 'action', 
      headerName: 'Action', 
      flex: 0.8,
      minWidth: 90,
      renderCell: (params) => (
        <Chip
          label={params.value || 'Unknown'}
          size="small"
          color={ACTION_COLORS[params.value] || 'default'}
          sx={{ fontSize: '0.75rem' }}
        />
      ),
    },
    { 
      field: 'temp', 
      headerName: 'Temp', 
      flex: 0.6,
      minWidth: 70,
      align: 'center',
      headerAlign: 'center',
      renderCell: (params) => (
        <Typography 
          variant="body2" 
          sx={{ 
            fontWeight: params.value !== '-' ? 600 : 400,
            color: params.value !== '-' ? 'primary.main' : 'text.secondary',
          }}
        >
          {params.value || '-'}
        </Typography>
      ),
    },
  ], []);

  const processedRows = useMemo(() => {
    return rows.map((row, index) => ({
      ...row,
      id: row.id || `task-${index}`,
    }));
  }, [rows]);

  const noRowsOverlay = () => (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100%',
      }}
    >
      <Typography variant="body2" color="text.secondary">
        No scheduled tasks
      </Typography>
    </Box>
  );

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="subtitle1" gutterBottom sx={{ mb: 2 }}>
        Scheduled Tasks
      </Typography>
      <DataGrid
        rows={processedRows}
        getRowId={(row) => row.id}
        columns={columns}
        autoHeight
        hideFooter
        disableColumnMenu
        disableSelectionOnClick
        rowHeight={52}
        headerHeight={48}
        slots={{
          noRowsOverlay: noRowsOverlay,
        }}
        sx={{
          border: 'none',
          '& .MuiDataGrid-main': {
            borderRadius: 1,
            border: (theme) => `1px solid ${theme.palette.divider}`,
          },
          '& .MuiDataGrid-cell': {
            borderBottom: (theme) => `1px solid ${theme.palette.divider}`,
            display: 'flex',
            alignItems: 'center',
          },
          '& .MuiDataGrid-columnHeaders': {
            backgroundColor: (theme) => 
              theme.palette.mode === 'dark' ? 'grey.900' : 'grey.50',
            borderBottom: (theme) => `1px solid ${theme.palette.divider}`,
          },
          '& .MuiDataGrid-columnHeaderTitle': {
            fontWeight: 600,
            fontSize: '0.875rem',
          },
          '& .MuiDataGrid-row:hover': {
            backgroundColor: (theme) =>
              theme.palette.mode === 'dark' ? 'grey.800' : 'grey.50',
          },
        }}
      />
    </Paper>
  );
}

ScheduleTable.propTypes = {
  rows: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string,
      taskName: PropTypes.string,
      mode: PropTypes.string,
      startTime: PropTypes.string,
      endTime: PropTypes.string,
      frequency: PropTypes.string,
      action: PropTypes.string,
      temp: PropTypes.string,
    })
  ),
};