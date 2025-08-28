import React, { useEffect, useState, useMemo, useCallback } from 'react';
import {
  Paper, Box, Typography, Stack, Button,
  FormControl, InputLabel, Select, MenuItem,
  Skeleton, Alert, Chip, IconButton, Tooltip as MuiTooltip
} from '@mui/material';
import {
  LineChart, Line, CartesianGrid, XAxis, YAxis, 
  Tooltip, ResponsiveContainer, Area, AreaChart
} from 'recharts';
import RefreshIcon from '@mui/icons-material/Refresh';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import PropTypes from 'prop-types';
import WebApi from '../../WebApi/WebApi';

// Constants
const CHART_HEIGHT = 300;
const DATE_FORMAT_OPTIONS = { weekday: 'short' };
const FULL_DATE_FORMAT_OPTIONS = { 
  month: 'short', 
  day: 'numeric', 
  year: 'numeric' 
};

// Custom tooltip component for the chart
const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    const data = payload[0];
    return (
      <Paper 
        sx={{ 
          p: 1.5, 
          backgroundColor: 'background.paper',
          border: 1,
          borderColor: 'divider',
        }}
        elevation={3}
      >
        <Typography variant="caption" color="text.secondary">
          {label}
        </Typography>
        <Typography variant="body2" fontWeight="bold" color="primary">
          {data.value} kWh
        </Typography>
      </Paper>
    );
  }
  return null;
};

CustomTooltip.propTypes = {
  active: PropTypes.bool,
  payload: PropTypes.array,
  label: PropTypes.string,
};

export default function EnergyChart({ onRefresh }) {
  const [selectedSite, setSelectedSite] = useState('');
  const [sites, setSites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [refreshing, setRefreshing] = useState(false);

  // Fetch sites on component mount
  useEffect(() => {
    fetchSites();
  }, []);

  const fetchSites = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const tenant = JSON.parse(localStorage.getItem('tenant'));
      const user = JSON.parse(localStorage.getItem('user'));

      if (!tenant || !user) {
        throw new Error('User session not found. Please login again.');
      }

      const fetchedSites = await WebApi.getSitesForTenant({
        tenantSystemID: tenant.id.systemID,
        tenantId: tenant.id.objectId,
        userSystemID: user.userId.systemID,
        userEmail: user.userId.email,
      });

      setSites(fetchedSites || []);

      // Set initial site selection
      if (fetchedSites.length > 0 && !selectedSite) {
        setSelectedSite(fetchedSites[0].id.objectId);
      }
    } catch (err) {
      console.error('Failed to fetch sites:', err);
      setError(err.message || 'Failed to load sites');
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await fetchSites();
    if (onRefresh) {
      onRefresh();
    }
    setRefreshing(false);
  }, [onRefresh]);

  const handleSiteChange = (event) => {
    setSelectedSite(event.target.value);
  };

  // Process energy data for the selected site
  const chartData = useMemo(() => {
    const site = sites.find((s) => s.id.objectId === selectedSite);
    
    if (!site || !site.objectDetails?.powerConsumptionLogs) {
      return [];
    }

    const logs = site.objectDetails.powerConsumptionLogs;
    
    // Sort logs by date and take last 7 days
    const sortedLogs = [...logs]
      .sort((a, b) => new Date(a.date) - new Date(b.date))
      .slice(-7);

    // logs
    
    return sortedLogs.map((log) => ({
      day: new Date(log.date).toLocaleDateString('en-US', DATE_FORMAT_OPTIONS),
      fullDate: new Date(log.date).toLocaleDateString('en-US', FULL_DATE_FORMAT_OPTIONS),
      kwh: log.kwh || 0,
      date: log.date,
    }));
  }, [sites, selectedSite]);

  // Calculate statistics
  const statistics = useMemo(() => {
    if (chartData.length === 0) {
      return { total: 0, average: 0, trend: 0, highest: 0, lowest: 0 };
    }

    const values = chartData.map(d => d.kwh);
    const total = values.reduce((sum, val) => sum + val, 0);
    const average = total / values.length;
    
    // Calculate trend (compare last 3 days to previous 3 days)
    let trend = 0;
    if (values.length >= 6) {
      const recent = values.slice(-3).reduce((a, b) => a + b, 0) / 3;
      const previous = values.slice(-6, -3).reduce((a, b) => a + b, 0) / 3;
      trend = previous > 0 ? ((recent - previous) / previous) * 100 : 0;
    }

    return {
      total: total.toFixed(1),
      average: average.toFixed(1),
      trend: trend.toFixed(1),
      highest: Math.max(...values).toFixed(1),
      lowest: Math.min(...values).toFixed(1),
    };
  }, [chartData]);

  // Render loading state
  if (loading) {
    return (
      <Paper sx={{ p: 2, pb: 3.5 }}>
        <Skeleton variant="text" width={200} height={30} />
        <Skeleton variant="rectangular" height={40} sx={{ mt: 2, mb: 2 }} />
        <Skeleton variant="rectangular" height={CHART_HEIGHT} />
      </Paper>
    );
  }

  // Render error state
  if (error) {
    return (
      <Paper sx={{ p: 2 }}>
        <Alert 
          severity="error" 
          action={
            <Button size="small" onClick={handleRefresh}>
              Retry
            </Button>
          }
        >
          {error}
        </Alert>
      </Paper>
    );
  }

  // Render empty state
  if (sites.length === 0) {
    return (
      <Paper sx={{ p: 2 }}>
        <Alert severity="info">
          No sites available. Please add a site to view energy consumption.
        </Alert>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2, pb: 3.5, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
        <Typography variant="subtitle1" fontWeight="medium">
          Energy Consumption (kWh)
        </Typography>
        <MuiTooltip title="Refresh data">
          <IconButton 
            size="small" 
            onClick={handleRefresh}
            disabled={refreshing}
          >
            <RefreshIcon 
              fontSize="small" 
              sx={{ 
                animation: refreshing ? 'spin 1s linear infinite' : 'none',
                '@keyframes spin': {
                  '0%': { transform: 'rotate(0deg)' },
                  '100%': { transform: 'rotate(360deg)' },
                },
              }} 
            />
          </IconButton>
        </MuiTooltip>
      </Box>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={2}>
        {/* Site Filter */}
        <FormControl size="small" fullWidth>
          <InputLabel>Filter by site</InputLabel>
          <Select
            value={selectedSite}
            label="Filter by site"
            onChange={handleSiteChange}
            disabled={refreshing}
          >
            {sites.map((site) => (
              <MenuItem key={site.id.objectId} value={site.id.objectId}>
                {site.alias || `Site ${site.id.objectId.slice(-6)}`}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {/* Statistics Chips */}
        <Stack direction="row" spacing={1} sx={{ minWidth: 200 }}>
          <Chip
            icon={<CalendarTodayIcon />}
            label={`Avg: ${statistics.average} kWh`}
            size="small"
            color="primary"
            variant="outlined"
          />
          <Chip
            icon={
              statistics.trend > 0 ? 
                <TrendingUpIcon /> : 
                <TrendingDownIcon />
            }
            label={`${Math.abs(statistics.trend)}%`}
            size="small"
            color={statistics.trend > 0 ? 'error' : 'success'}
            variant="outlined"
          />
        </Stack>
      </Stack>

      {/* Chart */}
      <Box sx={{ flex: 1, minHeight: 0 }}>
        {chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={CHART_HEIGHT}>
            <AreaChart 
              data={chartData}
              margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
            >
              <defs>
                <linearGradient id="colorKwh" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#1976d2" stopOpacity={0.8}/>
                  <stop offset="95%" stopColor="#1976d2" stopOpacity={0.1}/>
                </linearGradient>
              </defs>
              <CartesianGrid 
                strokeDasharray="3 3" 
                stroke="#e0e0e0"
                vertical={false}
              />
              <XAxis 
                dataKey="day" 
                tick={{ fontSize: 12 }}
                tickLine={false}
              />
              <YAxis 
                tick={{ fontSize: 12 }}
                tickLine={false}
                axisLine={false}
                tickFormatter={(value) => `${Number(value).toFixed(2)} kWh`}
              />
              <Tooltip content={<CustomTooltip />} />
              <Area
                type="monotone"
                dataKey="kwh"
                stroke="#1976d2"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#colorKwh)"
              />
            </AreaChart>
          </ResponsiveContainer>
        ) : (
          <Box 
            sx={{ 
              height: CHART_HEIGHT, 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center' 
            }}
          >
            <Typography variant="body2" color="text.secondary">
              No energy data available for the selected site
            </Typography>
          </Box>
        )}
      </Box>

      {/* Summary Statistics */}
      {chartData.length > 0 && (
        <Stack 
          direction="row" 
          spacing={2} 
          sx={{ 
            mt: 2, 
            pt: 2, 
            borderTop: 1, 
            borderColor: 'divider' 
          }}
        >
          <Box sx={{ flex: 1, textAlign: 'center' }}>
            <Typography variant="caption" color="text.secondary">
              Total (7 days)
            </Typography>
            <Typography variant="body2" fontWeight="bold">
              {statistics.total} kWh
            </Typography>
          </Box>
          <Box sx={{ flex: 1, textAlign: 'center' }}>
            <Typography variant="caption" color="text.secondary">
              Highest
            </Typography>
            <Typography variant="body2" fontWeight="bold">
              {statistics.highest} kWh
            </Typography>
          </Box>
          <Box sx={{ flex: 1, textAlign: 'center' }}>
            <Typography variant="caption" color="text.secondary">
              Lowest
            </Typography>
            <Typography variant="body2" fontWeight="bold">
              {statistics.lowest} kWh
            </Typography>
          </Box>
        </Stack>
      )}
    </Paper>
  );
}

EnergyChart.propTypes = {
  onRefresh: PropTypes.func,
};