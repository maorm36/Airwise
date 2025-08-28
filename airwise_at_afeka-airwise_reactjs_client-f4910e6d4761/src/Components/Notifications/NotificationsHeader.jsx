// import React from 'react';
// import { Stack, Typography, Chip } from '@mui/material';
// import MailOutlineIcon from '@mui/icons-material/MailOutline';
// import InfoIcon from '@mui/icons-material/Info';
// import WarningAmberIcon from '@mui/icons-material/WarningAmber';
// import SettingsIcon from '@mui/icons-material/Settings';

// export default function NotificationsHeader({ counts, isMobile }) {
//   return (
//     <Stack
//       direction={isMobile ? 'column' : 'row'}
//       alignItems="center"
//       justifyContent="space-between"
//       spacing={1}
//     >
//       <Typography variant="h5">Notifications</Typography>
//       <Stack direction="row" spacing={1}>
//         <Chip icon={<MailOutlineIcon />} label={`Unread: ${counts.unread}`} color="error" variant="outlined" />
//         <Chip icon={<InfoIcon />} label={`Info: ${counts.info}`} color="info" variant="outlined" />
//         <Chip icon={<WarningAmberIcon />} label={`Warnings: ${counts.warning}`} color="warning" variant="outlined" />
//         <Chip icon={<SettingsIcon />} label={`Maintenance: ${counts.maintenance}`} variant="outlined" />
//       </Stack>
//     </Stack>
//   );
// }