import React, { useMemo } from 'react';
import { Paper, Stack, Typography, Box, useTheme } from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import PropTypes from 'prop-types';

const MAX_VISIBLE_ROWS = 7;
const ROW_HEIGHT = 44;
const HEADER_HEIGHT = 56;

export default function GroupListHome({ rows = [], onSelect }) {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  // Memoize filled rows to avoid recalculation on every render
  const filledRows = useMemo(() => {
    const filled = [...rows];
    const missing = Math.max(0, MAX_VISIBLE_ROWS - rows.length);

    // Add empty rows if needed
    for (let i = 0; i < missing; i++) {
      filled.push({
        id: `empty-${i}`,
        Name: '',
        ACs: '',
        isEmpty: true,
      });
    }

    return filled;
  }, [rows]);

  // Memoize columns to avoid recreation on every render
  const columns = useMemo(() => [
    {
      field: 'Name',
      headerName: 'Site',
      flex: 1,
      minWidth: 120,
      cellClassName: 'left-column',
      renderCell: (params) => (
        <Typography variant="body2" noWrap>
          {params.value || '-'}
        </Typography>
      ),
    },
    {
      field: 'ACs',
      headerName: '# of ACs',
      flex: 1,
      minWidth: 130,
      cellClassName: 'right-column',
      renderCell: (params) => (
        <Typography variant="body2" align="center">
          {params.value !== '' ? params.value : '-'}
        </Typography>
      ),
    },
  ], []);

  // Memoize styles to avoid recreation
  const dataGridStyles = useMemo(() => ({
    height: '100%',
    border: `1px solid ${isDark ? '#444' : '#f0f0f0'}`,
    borderRadius: '4px',
    '& .MuiDataGrid-cell': {
      whiteSpace: 'normal',
      wordBreak: 'break-word',
      borderBottom: `1px solid ${isDark ? '#555' : '#c0c0c0'}`,
    },
    '& .MuiDataGrid-columnHeaders': {
      borderBottom: `1px solid ${isDark ? '#555' : '#c0c0c0'}`,
      backgroundColor: isDark ? '#1e1e1e' : undefined,
    },
    '& .MuiDataGrid-columnSeparator': {
      display: 'block',
      color: isDark ? '#555' : '#c0c0c0',
    },
    '& .MuiDataGrid-columnHeaderTitle': {
      whiteSpace: 'normal',
    },
    '& .left-column': {
      borderRight: `1px solid ${isDark ? '#555' : '#c0c0c0'}`,
    },
    '& .MuiDataGrid-row.empty-row': {
      backgroundColor: isDark ? '#121212' : '#fafafa',
      pointerEvents: 'none',
      '& .MuiDataGrid-cell': {
        color: 'transparent',
      },
    },
    '& .MuiDataGrid-virtualScroller': {
      overflow: 'hidden !important',
    },
  }), [isDark]);

  const handleRowClick = (params) => {
    if (!params.row.isEmpty && onSelect) {
      onSelect(params.row);
    }
  };

  return (
    <Paper
      sx={{
        height: '100%',
        pt: 2,
        pb: 2,
        px: 2,
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Stack direction="column" spacing={1} sx={{ height: '100%' }}>
        <Typography variant="subtitle1">Groups</Typography>

        <Box sx={{ flex: 1, minHeight: 0 }}>
          <DataGrid
            rows={filledRows}
            columns={columns}
            hideFooter
            disableColumnMenu
            autoHeight={false}
            rowHeight={ROW_HEIGHT}
            headerHeight={HEADER_HEIGHT}
            onRowClick={handleRowClick}
            getRowClassName={(params) =>
              params.row.isEmpty ? 'empty-row' : ''
            }
            sx={dataGridStyles}
          />
        </Box>
      </Stack>
    </Paper>
  );
}

GroupListHome.propTypes = {
  rows: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      Name: PropTypes.string,
      ACs: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    })
  ),
  onSelect: PropTypes.func,
};