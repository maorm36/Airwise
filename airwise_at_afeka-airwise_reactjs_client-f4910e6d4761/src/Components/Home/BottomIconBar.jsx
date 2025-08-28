import React from 'react';
import { Box, Stack, Typography } from '@mui/material';
import {
  AcUnit,
  Whatshot,
  Thermostat,
  EventNote,
  BarChart
} from '@mui/icons-material';

export default function BottomIconBar() {
  return (
    <Box
      sx={{
        mt: 4,
        p: 2,
        display: 'flex',
        justifyContent: 'space-around',
        flexWrap: 'wrap',
      }}
    >
      <Stack direction="row" spacing={14} justifyContent="center" alignItems="center" flexWrap="wrap">
        <Box>
          <Stack direction="row" alignItems="center" spacing={1}>
            <Typography variant="body1">Cooling</Typography>
            <AcUnit fontSize="large" />
          </Stack>
        </Box>

        <Box>
          <Stack direction="row" alignItems="center" spacing={1}>
            <Typography variant="body1">Heating</Typography>
            <Whatshot fontSize="large" />
          </Stack>
        </Box>

        <Box>
          <Stack direction="row" alignItems="center" spacing={1}>
            <Typography variant="body1">Temperature</Typography>
            <Thermostat fontSize="large" />
          </Stack>
        </Box>

        <Box>
          <Stack direction="row" alignItems="center" spacing={1}>
            <Typography variant="body1">Schedule</Typography>
            <EventNote fontSize="large" />
          </Stack>
        </Box>

        <Box>
          <Stack direction="row" alignItems="center" spacing={1}>
            <Typography variant="body1">Reports</Typography>
            <BarChart fontSize="large" />
          </Stack>
        </Box>
      </Stack>
    </Box>
  );
}
