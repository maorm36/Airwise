import React from 'react';
import { Paper, Stack, Typography, Button, Box, IconButton } from '@mui/material';
import { DataGrid, GridDeleteIcon } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { AcConstants } from '../Utils/AcConstants';

export default function RoomsTable({ rows, selectedSite, onAdd, onEditSingle, onSelect, onDeleteSingle, roomACCounts = {}, onControlAll }) {

  const columns = [
    {
      field: 'alias',
      headerName: 'Room',
      flex: 1
    },
    {
      field: 'active',
      headerName: '#Active ACs',
      flex: 1,
      renderCell: (params) => {
        if (!params || !params.row) return '0';

        const roomId = params.row.id?.objectId;
        const activeCount = roomACCounts[roomId] || 0;

        return <span style={{ fontWeight: 'bold', color: activeCount > 0 ? 'green' : 'gray' }}>{activeCount}</span>;
      }
    },
    {
      field: 'control',
      headerName: 'Control All ACs',
      flex: 1.5,
      renderCell: (params) => {
        if (!params || !params.row) return null;

        const roomId = params.row.id?.objectId;
        const activeCount = roomACCounts[roomId] || 0;

        return (
          <Stack direction="row" spacing={1}>
            <Button
              size="small"
              variant="contained"
              color="success"
              onClick={(e) => {
                e.stopPropagation();
                if (window.confirm(`Turn ON all ACs in room "${params.row.alias}"? This will apply room preferences to all ACs.`)) {
                  onControlAll && onControlAll(params.row, AcConstants.ACTIONS.TURN_ON);
                }
              }}
            >
              TURN ON ALL
            </Button>
            <Button
              size="small"
              variant="contained"
              color="error"
              disabled={activeCount === 0}
              onClick={(e) => {
                e.stopPropagation();
                if (window.confirm(`Turn OFF all ACs in room "${params.row.alias}"?`)) {
                  onControlAll && onControlAll(params.row, AcConstants.ACTIONS.TURN_OFF);
                }
              }}
            >
              TURN OFF ALL
            </Button>
          </Stack>
        );
      }
    },
    {
      field: 'controls',
      headerName: 'Controls',
      flex: 1,
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
              title="Edit Room Preferences"
            >
              <EditOutlinedIcon fontSize="small" />
            </IconButton>
            <IconButton
              size="small"
              color="error"
              onClick={(e) => {
                e.stopPropagation();
                const roomName = params.row.alias || 'this room';
                if (window.confirm(`Are you sure you want to delete room: ${roomName}?`)) {
                  onDeleteSingle && onDeleteSingle(params.row);
                }
              }}
              title="Delete Room"
            >
              <GridDeleteIcon fontSize="small" />
            </IconButton>
          </Stack>
        );
      }
    }
  ];


  return (
    <Paper sx={{ flex: 1, p: 2 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1}>
        <Typography variant="subtitle1">
          Rooms {rows?.length ? `(${rows.length})` : '(0)'}
        </Typography>
        <Button size="small" startIcon={<AddIcon />} onClick={onAdd}>Add Room</Button>
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
              { selectedSite ? 'No rooms in this site' : 'Please select a site to view rooms' }
            </Typography>
          </Box>
        ) : (
          <DataGrid
            getRowId={row => {
              return row.id?.objectId || row.objectId || row.id || `fallback-${Math.random().toString(36).substr(2, 9)}`;
            }}
            rows={rows}
            columns={columns}
            autoHeight
            hideFooter
            onRowClick={(params) => onSelect && params?.row && onSelect(params.row)}
            sx={{ cursor: 'pointer' }}
          />
        )}
      </Box>
    </Paper>
  );
}
