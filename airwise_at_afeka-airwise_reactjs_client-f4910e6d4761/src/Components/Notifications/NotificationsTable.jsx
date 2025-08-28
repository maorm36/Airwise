// import React, { useEffect } from 'react';
// import { Box } from '@mui/material';
// import { DataGrid } from '@mui/x-data-grid';
// import InfoIcon from '@mui/icons-material/Info';
// import WarningAmberIcon from '@mui/icons-material/WarningAmber';
// import SettingsIcon from '@mui/icons-material/Settings';

// export default function NotificationsTable({ rows, loading, selection, setSelection }) {
//   const columns = [
//     { field: 'date', headerName: 'Date', flex: 1 },
//     {
//       field: 'message', headerName: 'Message', flex: 2, renderCell: params => {
//         const icons = {
//           Info: <InfoIcon color="info" />, 
//           Warning: <WarningAmberIcon color="warning" />, 
//           Maintenance: <SettingsIcon color="action" />
//         };
//         return (
//           <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
//             {icons[params.row.type]}
//             {params.value}
//           </Box>
//         );
//       }
//     },
//     { field: 'type', headerName: 'Type', flex: 1 },
//     { field: 'location', headerName: 'Location', flex: 1 },
//     { field: 'status', headerName: 'Status', flex: 1 },
//   ];

//   return (
//     <Box sx={{ height: '100%', width: '100%' }}>
//       <DataGrid
//         rows={rows}
//         loading={loading}
//         columns={columns}
//         checkboxSelection={selection}        
//         onRowSelectionModelChange={setSelection}
//         disableColumnMenu
//         sx={{ border: 'none' }}
//       />
//     </Box>
//   );
// }

