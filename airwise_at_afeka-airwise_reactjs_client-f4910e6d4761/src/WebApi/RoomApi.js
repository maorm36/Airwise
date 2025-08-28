import api from './api';
import * as defaultACPreferences from '../defaultACPreferences.json';
import { AcConstants } from '../Components/Utils/AcConstants';


export async function getRoomsForSite({ siteSystemID, siteId, userSystemID, userEmail, size = 10, page = 0 }) {
  try {
    const response = await api.get(
      `/objects/${siteSystemID}/${siteId}/children`,
      { params: { userSystemID, userEmail, size, page } }
    );

    return response.data.filter(obj => obj.type === 'Room');
  } catch (error) {
    if (error.response?.status === 404) {
      return [];
    }
    throw error;
  }
}

export async function getSpecificRoom({ systemID, objectId, userSystemID, userEmail }) {
  const { data } = await api.get(
    `/objects/${systemID}/${objectId}`,
    { params: { userSystemID, userEmail } }
  );
  return data;
}

export async function updateRoomACPreferences({
  objectSystemID,
  objectId,
  updatedACPrefs,
  operatorSystemID,
  operatorEmail,
  room
}) {

  const objectBoundary = {
    type: 'Room',
    alias: updatedACPrefs.name ?? room.alias,
    active: room.active || true,
    status: room.status || AcConstants.STATUS.OFF,
    createdBy: room.createdBy || { userId: { systemID: operatorSystemID, email: operatorEmail } },
    objectDetails: {
      ...updatedACPrefs // Apply the new preferences
    }
  };

  const response = await api.put(
    `/objects/${objectSystemID}/${objectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
    objectBoundary
  );
  return response.data;
}

export async function createRoom({ operatorSystemID: operatorSystemID, operatorEmail: operatorEmail, room: room }) {
  const objectBoundary = {
    type: 'Room',
    alias: room.name,
    active: true,
    createdBy: { userId: { systemID: operatorSystemID, email: operatorEmail } },
    status: AcConstants.STATUS.OFF,
    objectDetails: {
      ...defaultACPreferences
    }
  };
  const response = await api.post('/objects', objectBoundary);
  return response.data;
}

export async function bindRoomToSite(SiteSystemID, SiteId, roomObjectId, operatorSystemID, operatorEmail) {
  const response = await api.put(
    `/objects/${SiteSystemID}/${SiteId}/children?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
    roomObjectId
  );
  return response.data;
}

export async function deleteRoom({ roomSystemID, roomObjectId, operatorSystemID, operatorEmail }) {

  const currentResponse = await api.get(
    `/objects/${roomSystemID}/${roomObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`
  );

  const currentRoom = currentResponse.data;

  // Create complete ObjectBoundary with all required fields preserved
  const updateData = {
    type: currentRoom.type,
    alias: currentRoom.alias,
    active: false, // This is what we want to change
    status: currentRoom.status,
    objectDetails: currentRoom.objectDetails,
    createdBy: currentRoom.createdBy
  };


  const response = await api.put(
    `/objects/${roomSystemID}/${roomObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
    updateData
  );

  return response.data;
}



export default {
  getRoomsForSite,
  createRoom,
  bindRoomToSite,
  updateRoomACPreferences,
  getSpecificRoom,
  deleteRoom
};