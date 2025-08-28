import React, { use, useEffect, useState } from 'react';
import { Box, Typography, useTheme } from '@mui/material';
import GroupListHome from './GroupListHome';
import SummaryCards from './SummaryCards';
import EnergyChart from './EnergyChart';
import ScheduleTable from './ScheduleTable';
import ActivityLog from './ActivityLog';
import WebApi from '../../WebApi/WebApi';
import {countActiveACsForSites} from '../ACControl/utils/ACCounterUtils';
import { ScheduleConstants } from '../Utils/ScheduleConstants';
import { AcConstants } from '../Utils/AcConstants';

export default function HomeScreen() {

  const [sites, setSites] = useState([]);
  const [scheduledTasks, setScheduledTasks] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [activeACs, setActiveACs] = useState(0);
  const theme = useTheme();
  theme.direction = "ltr"


  // fetch all of the sites with acs that is under the current tenant
  useEffect(() => {
    async function fetchSites() {
      try {
        const tenant = JSON.parse(localStorage.getItem('tenant'));
        const user = JSON.parse(localStorage.getItem('user'));

        const sitesForTenant = await WebApi.getSitesForTenant({
          tenantSystemID: tenant.id.systemID,
          tenantId: tenant.id.objectId,
          userSystemID: user.userId.systemID,
          userEmail: user.userId.email,
        });

        let sitesWithACCounts = await Promise.all(
          sitesForTenant.map(async (site, index) => {
            let totalACs = 0;

            try {
              const rooms = await WebApi.getRoomsForSite({
                siteSystemID: site.id.systemID,
                siteId: site.id.objectId,
                userSystemID: user.userId.systemID,
                userEmail: user.userId.email,
                size: 100,
                page: 0,
              });

              for (const room of rooms) {
                try {
                  const acs = await WebApi.getACsForRoom({
                    roomSystemID: room.id.systemID,
                    roomId: room.id.objectId,
                    userSystemID: user.userId.systemID,
                    userEmail: user.userId.email,
                    size: 100,
                    page: 0,
                  });
                  totalACs += acs.length;
                } catch (e) {
                  console.warn(`No ACs for room ${room.alias || room.id.objectId}`, e);
                }
              }
            } catch (e) {
              console.warn(`No rooms for site ${site.alias || site.id.objectId}`, e);
            }

            return {
              id: site.id.objectId || `site-${index}`,
              Name: site.alias || `Site ${index + 1}`,
              ACs: totalACs,
            };

          })
        );

        sitesWithACCounts.sitesForTenant = sitesForTenant;

        setSites(sitesWithACCounts);
      } catch (error) {
        console.error('Failed to fetch sites:', error);
      }
    }

    fetchSites();
  }, []);

  useEffect(() => {
    async function countActiveACs() {
      try {
        let totalActiveACs = 0;
        const user = JSON.parse(localStorage.getItem('user'));
        if (!sites.sitesForTenant || sites.sitesForTenant.length === 0) {
          console.warn('No sites available to count active ACs');
          setActiveACs(0);
          return;
        }
        const totalActiveACsObjectOfActiveAcsPerSite = await countActiveACsForSites(sites.sitesForTenant, user, WebApi);
        // loop over the object and sum the values
        if (!totalActiveACsObjectOfActiveAcsPerSite || Object.keys(totalActiveACsObjectOfActiveAcsPerSite).length === 0) {
          console.warn('No active ACs found for any site');
          setActiveACs(0);
          return;
        }
        for (const acCount of Object.values(totalActiveACsObjectOfActiveAcsPerSite)) {
            totalActiveACs += acCount;
        }
        setActiveACs(totalActiveACs);
      } catch (error) {
        console.error('Failed to count active ACs:', error);
      }
    }

    if (sites.length > 0) { // Only count if sites are fetched
      countActiveACs();
    }
  }, [sites]); // Re-run when sites change

// fetch Notifications
  useEffect(() => {
      async function fetchNotifications() {
        try {
          const tenant = JSON.parse(localStorage.getItem('tenant'));
          const user = JSON.parse(localStorage.getItem('user'));
          const notifications = await WebApi.getNotificationsForTenant({
            tenantSystemID: tenant.id.systemID,
            tenantId: tenant.id.objectId,
            userSystemID: user.userId.systemID,
            userEmail: user.userId.email,
            size: 5, // Adjust size as needed
            page: 0, // Start from the first page
          });


          const notis = notifications.map(notification => ({
            date: new Date(notification.creationTimestamp).toLocaleDateString(),
            time: new Date(notification.creationTimestamp).toLocaleTimeString(),
            text: notification.objectDetails.message,
            type: notification.status,
            alias: notification.alias || 'info',
            isEmpty: false, // Mark as not empty
          }));

          setNotifications(notis);

        } catch (error) {
          console.error('Failed to fetch notifications:', error);
        }
      }
    fetchNotifications();
  }, []);

  // fetch Scheduled Tasks
  useEffect(() => {
    async function fetchScheduledTasks() {
      try {
        const tenant = JSON.parse(localStorage.getItem('tenant'));
        const user = JSON.parse(localStorage.getItem('user'));

        // fetch tasks by alias "tasks-<tenantId>"
        let tasks = await WebApi.fetchTasksForTenant({
          tenantId: tenant.id.objectId,
          userSystemID: user.userId.systemID,
          userEmail: user.userId.email,
          size: 50, // Adjust size as needed
          page: 0, // Start from the first page
        });

        // sort tasks by the next scheduled time based on startTime and frequency
        // just tasks that have repeat frequency, or have status 'SCHEDULED'
        tasks = tasks.filter(task =>
          (task.objectDetails.repeat && task.objectDetails.repeat !== ScheduleConstants.FREQUENCIES.ONCE) ||
          task.objectDetails.status === 'SCHEDULED'
        );
        // sort tasks by startTime
        tasks.sort((a, b) => {
          const aTime = new Date(a.objectDetails.startTime || 0).getTime();
          const bTime = new Date(b.objectDetails.startTime || 0).getTime();
          return aTime - bTime;
        });

        // structure tasks to match ScheduleTable format
        const formattedTasks = tasks.map((task) => ({
          id: task.id.objectId,
          taskName: task.objectDetails.taskName,
          mode: task.objectDetails.mode || AcConstants.MODES.AUTO,
          startTime: task.objectDetails.startTime || '-',
          endTime: task.objectDetails.endTime || '-',
          frequency: task.objectDetails.repeat || ScheduleConstants.FREQUENCIES.ONCE,
          action: task.objectDetails.action || 'Unknown',
          temp: task.objectDetails.temperature ? `${task.objectDetails.temperature}°C` : '-',
        }));

        setScheduledTasks(formattedTasks);

      } catch (error) {
        console.error('Failed to fetch scheduled tasks:', error);
      }
    }

    fetchScheduledTasks();
  }, []);

  
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" mb={2}>
        <strong>Welcome {JSON.parse(localStorage.getItem('user')).username}!</strong>
      </Typography>

      <Box mb={3}>
        <SummaryCards data={{ activeACs: activeACs }} />
      </Box>

      <Box
        sx={{
          display: 'flex',
          gap: 2,
          flexDirection: {
            xs: 'column',
            md: 'row',
          },
          alignItems: 'stretch',
        }}
      >
        {/* LEFT SECTION */}
        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100%', gap: 2 }}>
          {/* Top row: Group + Chart */}
          <Box sx={{ display: 'flex', gap: 2, flex: 1 }}>
            <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
              <GroupListHome rows={sites} />
            </Box>
            <Box sx={{ flex: 2 }}>
              <EnergyChart />
            </Box>
          </Box>

          {/* Schedule section */}
          <Box>
            <Typography variant="subtitle1" gutterBottom>
              Next on Schedule
            </Typography>
            <ScheduleTable rows={scheduledTasks} />
          </Box>
        </Box>

        {/* NOTIFICATIONS SECTION */}
        <Box
          sx={{
            flex: '0 0 320px',
            height: '100%',
            display: 'stretch',
            flexDirection: 'column',
          }}
        >
        <ActivityLog notifications={notifications} fillEmptyRows targetRows={5} />

        </Box>
      </Box>
    </Box>
  );
}