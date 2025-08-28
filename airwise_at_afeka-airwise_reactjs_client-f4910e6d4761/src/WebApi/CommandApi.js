import { AcConstants } from '../Components/Utils/AcConstants';
import { AcState } from '../models/AcState';
import api from './api';

export async function invokeCommand({ command, targetObject, invokedBy, commandAttributes }) {
    const commandBoundary = {
        command: command,
        targetObject: targetObject,
        invokedBy: invokedBy,
        commandAttributes: commandAttributes || {}
    };

    try {
        const response = await api.post('/commands', commandBoundary);
        return response.data;
    } catch (error) {
        throw error;
    }
}

export async function updateACState({ acSystemID, acObjectId, newState, operatorSystemID, operatorEmail, preferences = null }) {
    // Create AcState object and convert to flat structure
    const acState = new AcState({
        power: newState === AcConstants.STATUS.ON,
        temperature: preferences?.temperature,
        mode: preferences?.mode,
        fanSpeed: preferences?.fanSpeed,
        serial: preferences?.serial || preferences?.serialNumber,
        manufacturer: preferences?.manufacturer,
        motion: preferences?.motion
    });

    // Validate the state
    const validation = acState.validate();
    if (!validation.valid) {
        throw new Error(`Invalid AC state: ${validation.errors.join(', ')}`);
    }

    // Convert to flat command attributes structure
    const commandAttributes = acState.toCommandAttributes();

    return invokeCommand({
        command: 'UPDATE_AC_STATE',
        targetObject: {
            id: {
                systemID: acSystemID,
                objectId: acObjectId
            }
        },
        invokedBy: {
            userId: {
                systemID: operatorSystemID,
                email: operatorEmail
            }
        },
        commandAttributes: commandAttributes
    });
}

export async function scheduleTask({ acSystemID, acObjectId, taskDetails, operatorSystemID, operatorEmail }) {
    const commandAttributes = {
        taskName: taskDetails.taskName,
        action: taskDetails.action,
        startTime: taskDetails.startTime,
        repeat: taskDetails.repeat,
        useCurrentPreferences: taskDetails.useCurrentPreferences
    };

    // Only add endTime if it exists
    if (taskDetails.endTime) {
        commandAttributes.endTime = taskDetails.endTime;
    }

    // Add AC settings directly to commandAttributes if not using current preferences
    if (!taskDetails.useCurrentPreferences) {
        commandAttributes.temperature = taskDetails.temperature;
        commandAttributes.mode = taskDetails.mode;
        commandAttributes.fanSpeed = taskDetails.fanSpeed;
    }

    return invokeCommand({
        command: 'SCHEDULE_TASK',
        targetObject: {
            id: {
                systemID: acSystemID,
                objectId: acObjectId
            }
        },
        invokedBy: {
            userId: {
                systemID: operatorSystemID,
                email: operatorEmail
            }
        },
        commandAttributes: commandAttributes
    });
}

export async function controlRoomACs({ roomSystemID, roomObjectId, action, operatorSystemID, operatorEmail, roomPreferences = null }) {
    // Create AcState from room preferences and convert to flat structure
    const acState = AcState.fromRoomPreferences(
        roomPreferences || {},
        action === AcConstants.ACTIONS.TURN_ON
    );

    // Validate the state
    const validation = acState.validate();
    if (!validation.valid) {
        throw new Error(`Invalid room AC preferences: ${validation.errors.join(', ')}`);
    }

    // Convert to flat command attributes structure
    const commandAttributes = acState.toCommandAttributes();

    return invokeCommand({
        command: 'ROOM_ACS_CONTROL',
        targetObject: {
            id: {
                systemID: roomSystemID,
                objectId: roomObjectId
            }
        },
        invokedBy: {
            userId: {
                systemID: operatorSystemID,
                email: operatorEmail
            }
        },
        commandAttributes: commandAttributes
    });
}

export async function verifyACBySerialThenAdd({ acSystemID, acObjectId, operatorSystemID, operatorEmail, acData }) {
    return invokeCommand({
        command: 'VERIFY_AC_BY_SERIAL_THEN_ADD',
        targetObject: {
            id: {
                systemID: acSystemID,
                objectId: acObjectId
            }
        },
        invokedBy: {
            userId: {
                systemID: operatorSystemID,
                email: operatorEmail
            }
        },
        commandAttributes: {
            serial: acData.serialNumber,
            manufacturer: acData.manufacturer,
            wattsOfDevice: acData.wattsOfDevice || 0, // Default to 0 if not provided
        }
    });
}

// make command method of DELETE_ENTITY_WITH_CHILDREN, entity -> Site, Room, AC, Task
export async function deleteEntityWithChildren({ entitySystemID, entityObjectId, operatorSystemID, operatorEmail }) {
    return invokeCommand({
        command: 'DELETE_ENTITY_WITH_CHILDREN',
        targetObject: {
            id: {
                systemID: entitySystemID,
                objectId: entityObjectId
            }
        },
        invokedBy: {
            userId: {
                systemID: operatorSystemID,
                email: operatorEmail
            }
        },
        commandAttributes: {}
    });
}

export default {
    invokeCommand,
    verifyACBySerialThenAdd,
    updateACState,
    scheduleTask,
    controlRoomACs,
    deleteEntityWithChildren
};
