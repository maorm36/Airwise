import React from 'react';
import { Paper, Stack, Typography, Button, Box, IconButton } from '@mui/material';
import { DataGrid, GridDeleteIcon } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import { AcConstants } from '../Utils/AcConstants';

export default function GroupsTable({ rows, onAdd, onSelect, onDeleteSingle, onEditSingle, siteACCounts = {} }) {

  const columns = [
    {
      field: 'alias',
      headerName: 'Site',
      flex: 1
    },
    {
      field: 'active',
      headerName: '#Active ACs',
      flex: 1,
      renderCell: (params) => {
        if (!params || !params.row) return '0';

        const siteId = params.row.id?.objectId;
        const activeCount = siteACCounts[siteId] || 0;

        return <span style={{ fontWeight: 'bold', color: activeCount > 0 ? 'green' : 'gray' }}>{activeCount}</span>;
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
              color="primary"
              onClick={(e) => {
                e.stopPropagation();
                onEditSingle && onEditSingle(params.row);
              }}
              title="Edit Site"
            >
              <EditIcon fontSize="small" />
            </IconButton>
            <IconButton
              size="small"
              color="error"
              onClick={(e) => {
                e.stopPropagation();
                const siteName = params.row.alias || 'this site';
                if (window.confirm(`Are you sure you want to delete site: ${siteName}?`)) {
                  onDeleteSingle && onDeleteSingle(params.row);
                }
              }}
              title="Delete Site"
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
          Groups {rows?.length ? `(${rows.length})` : '(0)'}
        </Typography>
        <Button size="small" startIcon={<AddIcon />} onClick={onAdd}>Add Site</Button>
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
              No sites for this tenant
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