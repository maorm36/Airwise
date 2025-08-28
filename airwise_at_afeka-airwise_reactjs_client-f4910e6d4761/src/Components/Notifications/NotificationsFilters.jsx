// import React from 'react';
// import { Stack, TextField, MenuItem, Button } from '@mui/material';

// export default function NotificationsFilters({
//   search, setSearch,
//   locationFilter, setLocationFilter,
//   startDate, setStartDate,
//   endDate, setEndDate,
//   locations,
//   isMobile
// }) {
//   return (
//     <Stack
//       direction={isMobile ? 'column' : 'row'}
//       spacing={2}
//       mt={2}
//       flexWrap="wrap"
//       marginTop={'30px'}
//     >
//       <TextField
//         size="small"
//         placeholder="Search"
//         value={search}
//         onChange={e => setSearch(e.target.value)}
//         sx={{ flex: 1, minWidth: 180 }}
//       />

//       <TextField
//         select
//         size="small"
//         value={locationFilter}
//         onChange={e => setLocationFilter(e.target.value)}
//         sx={{ width: 160 }}
//       >
//         {locations.map(loc => (
//           <MenuItem key={loc} value={loc}>{loc}</MenuItem>
//         ))}
//       </TextField>

//       <TextField
//         size="small"
//         type="date"
//         label="Start date"
//         InputLabelProps={{ shrink: true }}
//         value={startDate}
//         onChange={e => setStartDate(e.target.value)}
//       />

//       <TextField
//         size="small"
//         type="date"
//         label="End date"
//         InputLabelProps={{ shrink: true }}
//         value={endDate}
//         onChange={e => setEndDate(e.target.value)}
//       />

//       <Button
//         size="small"
//         variant="outlined"
//         onClick={() => {
//           setSearch('');
//           setLocationFilter('All Locations');
//           setStartDate('');
//           setEndDate('');
//         }}
//       >
//         Clear
//       </Button>
//     </Stack>
//   );
// }
