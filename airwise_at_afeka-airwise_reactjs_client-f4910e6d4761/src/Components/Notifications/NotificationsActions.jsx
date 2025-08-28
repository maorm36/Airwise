// import React from 'react';
// import { Stack, FormControlLabel, Checkbox, Button } from '@mui/material';

// export default function NotificationsActions({
//   autoRefresh,
//   setAutoRefresh,
//   onMarkRead,
//   onDelete,
//   onRefresh,
//   isMobile,
//   isButtonsDisabled
// }) {
  
//   return (
//     <Stack
//       direction={isMobile ? 'column' : 'row'}
//       alignItems="center"
//       justifyContent="space-between"
//       spacing={2}
//       mt={2}
//     >
//       <FormControlLabel
//         control={<Checkbox checked={autoRefresh} onChange={e => setAutoRefresh(e.target.checked)} />}
//         label="Auto-refresh every 30 sec"
//       />

//       <Stack direction="row" spacing={1}>
//         <Button variant="contained" disabled={isButtonsDisabled} onClick={onMarkRead}>
//           Mark as Read
//         </Button>
//         <Button variant="outlined" color="error" disabled={isButtonsDisabled} onClick={onDelete}>
//           Delete
//         </Button>
//         <Button variant="outlined" onClick={onRefresh}>
//           Refresh
//         </Button>
//       </Stack>
//     </Stack>
//   );
// }

