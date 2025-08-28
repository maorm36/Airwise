import React, { memo } from 'react';
import { Grid, Paper, Typography, Stack, Skeleton, Box } from '@mui/material';
import AcUnitIcon from '@mui/icons-material/AcUnit';
import AnnouncementIcon from '@mui/icons-material/Announcement';
import BarChartIcon from '@mui/icons-material/BarChart';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import PropTypes from 'prop-types';

const CARD_CONFIGS = {
  activeACs: {
    icon: AcUnitIcon,
    iconColor: 'primary',
    title: 'Operating AC Units',
    formatValue: (value) => `${value || 0} operating`,
    showTrend: false,
  },
  notifications: {
    icon: AnnouncementIcon,
    iconColor: 'warning',
    title: 'Notifications',
    formatValue: (value) => `${value || 0} pending`,
    showTrend: false,
  },
  energyUsage: {
    icon: BarChartIcon,
    iconColor: 'info',
    title: 'Energy Usage',
    formatValue: (value) => value || 'N/A',
    showTrend: true,
  },
};

// Memoized card component for performance
const SummaryCard = memo(({ config, value, trend, loading }) => {
  const Icon = config.icon;
  
  return (
    <Paper 
      elevation={2} 
      sx={{ 
        p: 2,
        height: '100%',
        transition: 'all 0.3s ease',
        '&:hover': {
          elevation: 4,
          transform: 'translateY(-2px)',
        },
      }}
    >
      {loading ? (
        <Stack spacing={1}>
          <Skeleton variant="circular" width={40} height={40} />
          <Skeleton variant="text" width="60%" />
          <Skeleton variant="text" width="80%" />
        </Stack>
      ) : (
        <Stack spacing={1.5}>
          <Stack direction="row" spacing={2} alignItems="flex-start">
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 48,
                height: 48,
                borderRadius: 2,
                backgroundColor: (theme) => 
                  theme.palette.mode === 'dark' 
                    ? `${theme.palette[config.iconColor].dark}20`
                    : `${theme.palette[config.iconColor].light}20`,
              }}
            >
              <Icon 
                fontSize="medium" 
                color={config.iconColor}
              />
            </Box>
            <Stack sx={{ flex: 1 }}>
              <Typography 
                variant="caption" 
                color="text.secondary"
                sx={{ 
                  textTransform: 'uppercase',
                  letterSpacing: 0.5,
                  fontWeight: 500,
                }}
              >
                {config.title}
              </Typography>
              <Stack direction="row" alignItems="baseline" spacing={1}>
                <Typography 
                  variant="h5" 
                  fontWeight="bold"
                  sx={{ lineHeight: 1.2 }}
                >
                  {config.formatValue(value)}
                </Typography>
                {config.showTrend && trend !== undefined && (
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    {trend > 0 ? (
                      <TrendingUpIcon 
                        sx={{ fontSize: 16, color: 'success.main' }} 
                      />
                    ) : (
                      <TrendingDownIcon 
                        sx={{ fontSize: 16, color: 'error.main' }} 
                      />
                    )}
                    <Typography 
                      variant="caption" 
                      color={trend > 0 ? 'success.main' : 'error.main'}
                      fontWeight="medium"
                    >
                      {Math.abs(trend)}%
                    </Typography>
                  </Stack>
                )}
              </Stack>
            </Stack>
          </Stack>
        </Stack>
      )}
    </Paper>
  );
});

SummaryCard.displayName = 'SummaryCard';

SummaryCard.propTypes = {
  config: PropTypes.shape({
    icon: PropTypes.elementType.isRequired,
    iconColor: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    formatValue: PropTypes.func.isRequired,
    showTrend: PropTypes.bool,
  }).isRequired,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  trend: PropTypes.number,
  loading: PropTypes.bool,
};

export default function SummaryCards({ data = {}, loading = false }) {
  // Determine which cards to show based on available data
  const visibleCards = Object.entries(CARD_CONFIGS).filter(
    ([key]) => data.hasOwnProperty(key)
  );

  if (visibleCards.length === 0) {
    return null;
  }

  // Calculate grid columns based on number of cards
  const getGridCols = (cardsCount) => {
    switch (cardsCount) {
      case 1:
        return { xs: 12, sm: 12, md: 12 };
      case 2:
        return { xs: 12, sm: 6, md: 6 };
      default:
        return { xs: 12, sm: 6, md: 4 };
    }
  };

  const gridCols = getGridCols(visibleCards.length);

  return (
    <Grid container spacing={2}>
      {visibleCards.map(([key, config]) => (
        <Grid item {...gridCols} key={key}>
          <SummaryCard
            config={config}
            value={data[key]}
            trend={data[`${key}Trend`]}
            loading={loading}
          />
        </Grid>
      ))}
    </Grid>
  );
}

SummaryCards.propTypes = {
  data: PropTypes.shape({
    activeACs: PropTypes.number,
    notifications: PropTypes.number,
    energyUsage: PropTypes.string,
    activeACsTrend: PropTypes.number,
    notificationsTrend: PropTypes.number,
    energyUsageTrend: PropTypes.number,
  }),
  loading: PropTypes.bool,
};