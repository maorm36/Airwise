import WebApi from '../../../WebApi/WebApi';
import { AcConstants } from '../../Utils/AcConstants';

export const useRoomHandlers = ({
    operator, user, selectedSite,
    refreshRooms, setRooms, closePanel,
    refreshSites, refreshACs, fetchTasks,
    showAlert
}) => {

    const handleAddRoom = async (data) => {
        try {
            const createdRoom = await WebApi.createRoom({
                operatorSystemID: operator.userId.systemID,
                operatorEmail: operator.userId.email,
                room: data
            });

            if (!createdRoom) {
                throw new Error('Room creation failed');
            }

            await WebApi.bindRoomToSite(
                selectedSite.id.systemID,
                selectedSite.id.objectId,
                { childId: { objectId: createdRoom.id.objectId, systemID: createdRoom.id.systemID } },
                operator.userId.systemID,
                operator.userId.email
            );

            await refreshRooms();
            showAlert('Room created successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error creating room', err);
                showAlert('Failed to create room', 'error');
            }
        } finally {
            closePanel();
        }
    };

    const applyRoomAcPrefs = async (data, room) => {
        try {
            await WebApi.updateRoomACPreferences({
                objectSystemID: room.id.systemID,
                objectId: room.id.objectId,
                updatedACPrefs: data,
                operatorSystemID: operator.userId.systemID,
                operatorEmail: operator.userId.email,
                room: room
            });

            const refreshedRoom = await WebApi.getSpecificRoom({
                systemID: room.id.systemID,
                objectId: room.id.objectId,
                userSystemID: user.userId.systemID,
                userEmail: user.userId.email
            });

            if (!refreshedRoom) {
                throw new Error('Failed to update room preferences');
            }

            setRooms(prev => {
                return prev.map(r =>
                    r.id.systemID === refreshedRoom.id.systemID &&
                        r.id.objectId === refreshedRoom.id.objectId
                        ? refreshedRoom
                        : r
                );
            });

            showAlert('Room preferences updated successfully!', 'success'); // ✅
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error updating room preferences', err);
                showAlert('Failed to update room preferences', 'error'); // ✅
            }
        } finally {
            closePanel();
        }
    };

    const handleDeleteRoom = async (room) => {
        try {
            await WebApi.deleteEntityWithChildren({
                entitySystemID: room.id.systemID,
                entityObjectId: room.id.objectId,
                operatorSystemID: user.userId.systemID,
                operatorEmail: user.userId.email
            });

            await refreshRooms();
            await refreshACs();
            await fetchTasks();

            showAlert('Room deleted successfully!', 'success');
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Error deleting room', err);
                showAlert('Failed to delete room', 'error');
            }
        }
    };

    const handleControlAllACsInRoom = async (room, action) => {
        try {
            const roomPreferences = {
                temperature: room.objectDetails.temperature,
                mode: room.objectDetails.mode,
                fanSpeed: room.objectDetails.fanSpeed
            };

            await WebApi.controlRoomACs({
                roomSystemID: room.id.systemID,
                roomObjectId: room.id.objectId,
                action: action,
                operatorSystemID: user.userId.systemID,
                operatorEmail: user.userId.email,
                roomPreferences: roomPreferences
            });

            showAlert(
                `Room ACs ${action === AcConstants.ACTIONS.TURN_ON ? 'turned on' : 'turned off'} successfully`,
                'success'
            );

            await refreshRooms();
            await refreshSites();
            await refreshACs();
        } catch (err) {
            if (err.response && err.response.data) {
                showAlert(`Error: ${err.response.data.message}`, 'error');
            } else {
                console.error('Failed to control ACs in room:', err);
                showAlert(`Failed to control ACs in room: ${err.message}`, 'error');
            }
        }
    };

    return {
        handleAddRoom,
        applyRoomAcPrefs,
        handleDeleteRoom,
        handleControlAllACsInRoom
    };
};
