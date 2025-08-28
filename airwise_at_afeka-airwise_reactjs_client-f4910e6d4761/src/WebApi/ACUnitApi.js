import { AcConstants } from '../Components/Utils/AcConstants';
import api from './api';

export async function getACsForRoom({ roomSystemID, roomId, userSystemID, userEmail, size = 10, page = 0 }) {

  try {
    const response = await api.get(
      `/objects/${roomSystemID}/${roomId}/children`,
      { params: { userSystemID, userEmail, size, page } }
    );
    // Server automatically filters by active status based on user role
    const acs = response.data.filter(obj => obj.type === 'AirConditioner');
    return acs;
  } catch (error) {
    if (error.response?.status === 404) {
      return [];
    }
    throw error;
  }
}

export async function createACUnit({ operatorSystemID, operatorEmail, ac, roomDefaults }) {
  const objectBoundary = {
    type: 'AirConditioner',
    alias: ac.serialNumber,
    active: true,
    createdBy: {
      userId: {
        systemID: operatorSystemID,
        email: operatorEmail
      }
    },
    status: AcConstants.STATUS.OFF,
    objectDetails: {
      // Basic AC information
      manufacturer: ac.manufacturer,
      serial: ac.serialNumber,
      watts: ac.wattsOfDevice || 0, // Default to 0 if not provided

      // Inherit room's AC preferences if provided
      temperature: roomDefaults?.temperature || 25,
      mode: roomDefaults?.mode || AcConstants.MODES.COOL,
      fanSpeed: roomDefaults?.fanSpeed || AcConstants.FAN_SPEEDS.AUTO,

      // AC-specific properties
      power: AcConstants.STATUS.OFF,
      energyConsumption: 0,

      // Room inheritance tracking (optional)
      inheritedFromRoom: roomDefaults ? true : false,
      roomInheritanceTimestamp: roomDefaults ? new Date().toISOString() : null
    }
  };


  const response = await api.post('/objects', objectBoundary);

  return response.data;
}

export async function bindACToRoom(roomSystemID, roomId, ACObjectId, operatorSystemID, operatorEmail) {

  const response = await api.put(
    `/objects/${roomSystemID}/${roomId}/children?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
    ACObjectId
  );

  return response.data;
}

export async function deleteACUnit({ acSystemID, acObjectId, operatorSystemID, operatorEmail }) {

  // First, get the current AC data to preserve required fields
  const currentResponse = await api.get(
    `/objects/${acSystemID}/${acObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`
  );

  const currentAC = currentResponse.data;

  // Create complete ObjectBoundary with all required fields preserved
  const updateData = {
    type: currentAC.type,
    alias: currentAC.alias,
    active: false, // This is what we want to change
    status: currentAC.status,
    objectDetails: currentAC.objectDetails,
    createdBy: currentAC.createdBy
  };

  const response = await api.put(
    `/objects/${acSystemID}/${acObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
    updateData
  );

  return response.data;
}

export async function updateACPreferences({ acSystemID, acObjectId, preferences, operatorSystemID, operatorEmail }) {

  // Get current AC data first to preserve required fields
  const currentResponse = await api.get(
    `/objects/${acSystemID}/${acObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`
  );

  const currentAC = currentResponse.data;

  // Prepare the updated object details
  let updatedObjectDetails = {
    ...currentAC.objectDetails,
    // Update the base preferences
    temperature: preferences.temperature || currentAC.objectDetails.temperature,
    mode: preferences.mode || currentAC.objectDetails.mode,
    fanSpeed: preferences.fanSpeed || currentAC.objectDetails.fanSpeed
  };

  // Create complete ObjectBoundary with all required fields preserved
  const updateData = {
    type: currentAC.type,
    alias: currentAC.alias,
    active: currentAC.active,
    status: currentAC.status,
    createdBy: currentAC.createdBy,
    objectDetails: updatedObjectDetails
  };


  const response = await api.put(
    `/objects/${acSystemID}/${acObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
    updateData
  );

  return response.data;
}


export default {
  getACsForRoom,
  createACUnit,
  bindACToRoom,
  deleteACUnit,
  updateACPreferences
};