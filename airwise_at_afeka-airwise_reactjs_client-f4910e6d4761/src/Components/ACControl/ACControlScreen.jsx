import { Box, Typography, useTheme, useMediaQuery } from '@mui/material';
import SidePanel from '../SidePanel/SidePanel';
import AddSiteForm from '../SidePanel/Forms/AddSiteForm';
import AddRoomForm from '../SidePanel/Forms/AddRoomForm';
import AddACForm from '../SidePanel/Forms/AddACForm';
import ACPreferencesForm from '../SidePanel/Forms/ACPreferencesForm';
import ScheduleTaskForm from '../SidePanel/Forms/ScheduleTaskForm';
import GroupsTable from './GroupsTable';
import RoomsTable from './RoomsTable';
import ACTable from './ACTable';
import TasksTable from './TasksTable';

import { useACControlData } from './hooks/useACControlData';
import { useSidePanel } from './hooks/useSidePanel';
import { useSiteHandlers } from './handlers/useSiteHandlers';
import { useRoomHandlers } from './handlers/useRoomHandlers';
import { useACHandlers } from './handlers/useACHandlers';
import { useTaskHandlers } from './handlers/useTaskHandlers';
import { ScheduleConstants } from '../Utils/ScheduleConstants';
import { AcConstants } from '../Utils/AcConstants';
import UpdateSiteForm from '../SidePanel/Forms/UpdateSiteForm'
import { useAlert } from '../../Components/AlertContext';


export default function ACControlScreen() {
  
  const theme = useTheme();
  const { showAlert } = useAlert();

  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const {
    sites, rooms, acUnits, tasks,
    siteACCounts, roomACCounts,
    isCountingACs,
    selectedSite, selectedRoom, selectedAC,
    setSelectedSite, setSelectedRoom, setSelectedAC,
    setSites, setRooms, setAcUnits, setTasks,
    refreshSites, refreshRooms, refreshACs,
    user, operator, tenant
  } = useACControlData();

    const {
    panelOpen,
    panelContent,
    panelTitle,
    openPanel,
    closePanel
  } = useSidePanel();

  const taskHandlers = useTaskHandlers({
    selectedAC, setTasks, closePanel, user, operator, tenant, showAlert
  });

  const fetchTasks = taskHandlers.fetchTasks;



  const siteHandlers = useSiteHandlers({
    operator, tenant, user, refreshSites, closePanel, refreshRooms, refreshACs, fetchTasks, showAlert
  });

  const roomHandlers = useRoomHandlers({
    operator, user, selectedSite, refreshRooms, setRooms, closePanel, refreshSites, refreshACs, fetchTasks, showAlert
  });

  const acHandlers = useACHandlers({
    operator, user, selectedRoom, refreshACs, closePanel, fetchTasks, showAlert 
  });

  const handleSiteSelect = (row) => {
    setSelectedSite(row);
    setSelectedRoom(null);
    setSelectedAC(null);
  };

  const handleRoomSelect = (row) => {
    setSelectedRoom(row);
    setSelectedAC(null);
  };

  const handleACSelect = (row) => {
    setSelectedAC(row);
  };

  const handleOpenAddSite = () => {
    openPanel(<AddSiteForm onSubmit={siteHandlers.handleAddSite} />, 'Add Site');
  };

  const handleOpenEditSite = (site) => {
    setSelectedRoom(site);
    openPanel(<UpdateSiteForm site={site} onSubmit={siteHandlers.handleUpdateSite} onCancel={closePanel} />, 'Edit Site')
  }

  const handleOpenAddRoom = () => {
    if (!selectedSite) return alert('Select a site first');
    openPanel(
      <AddRoomForm
        siteId={selectedSite.id.objectId}
        onSubmit={roomHandlers.handleAddRoom}
      />,
      'Add Room'
    );
  };

  const handleOpenAddAC = () => {
    if (!selectedRoom) return alert('Select a room first');
    openPanel(
      <AddACForm
        roomId={selectedRoom.id.objectId}
        onSubmit={acHandlers.handleAddAC}
      />,
      'Add AC Unit'
    );
  };

  const handleOpenAddTask = () => {
    if (!selectedAC) return alert('Select an AC unit first');
    openPanel(
      <ScheduleTaskForm
        onSubmit={taskHandlers.handleAddTask}
        selectedAC={selectedAC}
      />,
      'Schedule Task'
    );
  };

  const handleEditRoomPreferences = (room) => {
    setSelectedRoom(room);
    openPanel(
      <ACPreferencesForm
        type={"RoomPreferences"}
        entity={room}
        onSubmit={(data) => roomHandlers.applyRoomAcPrefs(data, room)}
      />,
      'Edit Room Details'
    );
  };

  const handleEditACPreferences = (ac) => {
    setSelectedAC(ac);
    openPanel(
      <ACPreferencesForm
        type={"ACPreferences"}
        entity={ac}
        onSubmit={(data) => acHandlers.handleEditACPreferences(ac, data)}
      />,
      'Edit AC Preferences'
    );
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, p: 3 }}>
      <Typography variant="h5">
        AC Control
        {isCountingACs && (
          <Typography component="span" variant="body2" sx={{ ml: 2, color: 'text.secondary' }}>
            (Counting active ACs...)
          </Typography>
        )}
      </Typography>

      <Box sx={{ display: 'flex', gap: 3 }}>
        <GroupsTable
          rows={sites}
          onAdd={handleOpenAddSite}
          onSelect={handleSiteSelect}
          onDeleteSingle={siteHandlers.handleDeleteSite}
          onEditSingle={handleOpenEditSite}
          siteACCounts={siteACCounts}
        />

        <RoomsTable
          rows={rooms}
          selectedSite={selectedSite}
          onAdd={handleOpenAddRoom}
          onSelect={handleRoomSelect}
          onDeleteSingle={roomHandlers.handleDeleteRoom}
          onEditSingle={handleEditRoomPreferences}
          onControlAll={roomHandlers.handleControlAllACsInRoom}
          roomACCounts={roomACCounts}
        />
      </Box>

      <ACTable
        rows={acUnits}
        selectedRoom={selectedRoom}
        onAdd={handleOpenAddAC}
        onDeleteSingle={acHandlers.handleDeleteAC}
        onSelect={handleACSelect}
        onTogglePower={acHandlers.handleToggleACPower}
        onEditSingle={handleEditACPreferences}
      />

      <TasksTable
        rows={tasks}
        selectedAC={selectedAC}
        onAdd={handleOpenAddTask}
        onDelete={taskHandlers.handleDeleteTask}
        onEdit={(task) => {

          openPanel(
            <ScheduleTaskForm
              onSubmit={(data) => taskHandlers.handleUpdateTask(task, {
                taskName: data.taskName,
                details: {
                  action: data.action,
                  startTime: data.startTime,
                  endTime: data.endTime,
                  repeat: data.repeat,
                  useCurrentPreferences: data.useCurrentPreferences,
                  temperature: data.temp,
                  mode: data.mode,
                  fanSpeed: data.fanSpeed
                }
              })}
              selectedAC={selectedAC}
              initialData={{
                taskName: task.objectDetails?.name,
                action: task.objectDetails?.action || ScheduleConstants.ACTIONS.TURN_ON,
                startTime: task.objectDetails?.startTime || '',
                endTime: task.objectDetails?.endTime || '',
                repeat: task.objectDetails?.repeat || ScheduleConstants.FREQUENCIES.EVERY_DAY,
                useCurrentPreferences: task.objectDetails?.useCurrentPreferences || true,
                temp: task.objectDetails?.temperature || 24,
                mode: task.objectDetails?.mode || AcConstants.MODES.COOL,
                fanSpeed: task.objectDetails?.fanSpeed || AcConstants.FAN_SPEEDS.MEDIUM
              }}
            />,
            'Edit Task'
          );
        }}
        onToggleTask={taskHandlers.handleToggleTask}
      />

      <SidePanel
        open={panelOpen}
        onClose={closePanel}
        title={panelTitle}
      >
        {panelContent}
      </SidePanel>
    </Box>
  );
}
