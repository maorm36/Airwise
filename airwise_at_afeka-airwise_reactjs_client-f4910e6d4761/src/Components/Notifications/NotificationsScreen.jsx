// import React, { useState, useMemo, useEffect } from 'react';
// import { Box, Paper, useTheme, useMediaQuery } from '@mui/material';
// import NotificationsHeader from './NotificationsHeader';
// import NotificationsFilters from './NotificationsFilters';
// import NotificationsTable from './NotificationsTable';
// import NotificationsActions from './NotificationsActions';
// import useNotifications from './useNotifications';

// export default function NotificationsScreen() {
//   const theme = useTheme();
//   const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

//   const [search, setSearch] = useState('');
//   const [locationFilter, setLocationFilter] = useState('All Locations');
//   const [startDate, setStartDate] = useState('');
//   const [endDate, setEndDate] = useState('');
//   const [autoRefresh, setAutoRefresh] = useState(true);
//   const [selection, setSelection] = useState([]);
//   const [isButtonsDisabled, setIsButtonsDisabled] = useState(true);

//   useEffect(() => {
//     if (selection && selection.ids && selection.ids.size > 0) {
//       setIsButtonsDisabled(false);
//     } else {
//       setIsButtonsDisabled(true);
//     }
//   } , [selection]);
  
//   const { rows, loading, counts, reload } = useNotifications({ search, locationFilter, startDate, endDate, autoRefresh });

//   const locations = useMemo(
//     () => ['All Locations', ...new Set(rows.map(r => r.location))],
//     [rows]
//   );

//   const handleMarkRead = () => {
//     // TODO: call API to mark `selection` as read, then `reload()`
//   };
//   const handleDelete = () => {
//     // TODO: call API to delete `selection`, then `reload()`
//   };

//   return (
//     <Box
//       component={Paper}
//       sx={{ p: { xs: 2, md: 3 }, display: 'flex', flexDirection: 'column', height: '100%' }}
//     >
//       <NotificationsHeader counts={counts} isMobile={isMobile} />
//       <NotificationsFilters
//         search={search} setSearch={setSearch}
//         locationFilter={locationFilter} setLocationFilter={setLocationFilter}
//         startDate={startDate} setStartDate={setStartDate}
//         endDate={endDate} setEndDate={setEndDate}
//         locations={locations} isMobile={isMobile}
//       />

//       <Box sx={{ flex: 1, mt: 2 }}>
//         <NotificationsTable
//           rows={rows}
//           loading={loading}
//           selection={selection} setSelection={setSelection}
//         />
//       </Box>

//       <NotificationsActions
//         autoRefresh={autoRefresh}
//         setAutoRefresh={setAutoRefresh}
//         selection={selection}
//         onMarkRead={handleMarkRead}
//         onDelete={handleDelete}
//         onRefresh={reload}
//         isMobile={isMobile}
//         isButtonsDisabled={isButtonsDisabled}
//       />
//     </Box>
//   );
// }
