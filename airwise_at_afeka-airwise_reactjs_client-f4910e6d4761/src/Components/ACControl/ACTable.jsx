import React from 'react';
import { Paper, Stack, Typography, Button, IconButton, Box } from '@mui/material';
import { DataGrid, GridDeleteIcon } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { AcConstants } from '../Utils/AcConstants';

export default function ACTable({ rows, selectedRoom, onAdd, onDeleteSingle, onSelect, onTogglePower, onEditSingle }) {
  const columns = [
    {
      field: 'status',
      headerName: 'Power Control',
      flex: 1,
      renderCell: (params) => {
        if (!params || !params.row) return null;

        const isOn = params.row.status === AcConstants.STATUS.ON;
        return (
          <Button
            variant="contained"
            size="small"
            color={isOn ? "error" : "success"}
            onClick={(e) => {
              e.stopPropagation();
              onTogglePower && onTogglePower(params.row);
            }}
          >

            {isOn ? "TURN OFF" : "TURN ON"}  {/**this is the text of the button!! not what we pass to the server */}
          </Button>
        );
      }
    },
    {
      field: 'power',
      headerName: 'Power Status',
      flex: 1,
      renderCell: (params) => {
        if (!params || !params.row) return 'Unknown';
        const status = params.row.status;
        return status === AcConstants.STATUS.ON ? 'ON' : status === AcConstants.STATUS.OFF ? 'OFF' : 'Unknown';
      }
    },
    {
      field: 'alias',
      headerName: 'Title/ID',
      flex: 1,
      renderCell: (params) => {
        if (!params || !params.row) return 'N/A';
        return params.row.alias || 'N/A';
      }
    },
    {
      field: 'temp',
      headerName: 'Temperature',
      flex: 1,
      renderCell: (params) => {
        if (!params || !params.row) return 'N/A';
        const currentTemp = params.row.status === AcConstants.STATUS.ON && params.row.objectDetails?.currentSession?.temperature
          ? params.row.objectDetails.currentSession.temperature
          : params.row.objectDetails?.temperature;
        return currentTemp ? `${currentTemp}°` : 'N/A';
      }
    },
    {
      field: 'mode',
      headerName: 'Mode',
      flex: 1,
      renderCell: (params) => {
        if (!params || !params.row) return 'N/A';
        const currentMode = params.row.status === AcConstants.STATUS.ON && params.row.objectDetails?.currentSession?.mode
          ? params.row.objectDetails.currentSession.mode
          : params.row.objectDetails?.mode;
        return currentMode || 'N/A';
      }
    },
    {
      field: 'fan',
      headerName: 'Fan Speed',
      flex: 1,
      renderCell: (params) => {
        if (!params || !params.row) return 'N/A';
        const currentFanSpeed = params.row.status === AcConstants.STATUS.ON && params.row.objectDetails?.currentSession?.fanSpeed
          ? params.row.objectDetails.currentSession.fanSpeed
          : params.row.objectDetails?.fanSpeed;
        return currentFanSpeed || 'N/A';
      }
    },
    {
      field: 'controls',
      headerName: 'Controls',
      flex: 1.5,
      renderCell: (params) => {
        if (!params || !params.row) return null;

        return (
          <Stack direction="row" spacing={1}>
            <IconButton
              size="small"
              onClick={(e) => {
                e.stopPropagation();
                onEditSingle && onEditSingle(params.row);
              }}
              title="Edit AC Preferences"
            >
              <EditOutlinedIcon fontSize="small" />
            </IconButton>
            <IconButton
              size="small"
              color="error"
              onClick={(e) => {
                e.stopPropagation();
                if (window.confirm(`Are you sure you want to delete AC: ${params.row.alias}?`)) {
                  onDeleteSingle && onDeleteSingle(params.row);
                }
              }}
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
          Air Conditioners {rows?.length ? `(${rows.length})` : '(0)'}
        </Typography>
        <Stack direction="row" spacing={1}>
          <Button size="small" variant="outlined" startIcon={<AddIcon />} onClick={onAdd}>
            Add AC
          </Button>
        </Stack>
      </Stack>
      <Box sx={{ minHeight: 300, width: '100%' }}>
        {/* Show a simple message if no data instead of trying to render DataGrid with empty/invalid data */}
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
              {selectedRoom ? 'No AC units found in this room.' : 'Please Select Room.'}
            </Typography>
          </Box>
        ) : (
          <DataGrid
            getRowId={row => {
              if (row?.id?.objectId) return row.id.objectId;
              if (row?.objectId) return row.objectId;
              if (row?.id) return row.id;
              return `fallback-${Math.random().toString(36).substr(2, 9)}`;
            }}
            rows={rows}
            columns={columns}
            autoHeight
            hideFooter
            onRowClick={(params) => onSelect && params?.row && onSelect(params.row)}
            sx={{ cursor: onSelect ? 'pointer' : 'default' }}
          />
        )}
      </Box>
    </Paper>
  );
}