import api from './api'
import { ScheduleConstants } from '../Components/Utils/ScheduleConstants';

export async function getTasksForAC({ ACSystemID, ACId, userSystemID, userEmail, size = 50, page = 0 }) {
    try {
        const response = await api.get(
            `/objects/${ACSystemID}/${ACId}/children?userSystemID=${userSystemID}&userEmail=${userEmail}&size=${size}&page=${page}`,
        );

        return response.data.filter(obj => obj.type === 'Task');
    } catch (error) {
        if (error.response?.status === 404) {
            return [];
        }

        throw error;
    }
}

export async function createTask({ operatorSystemID, operatorEmail, task, tenantId }) {
    const objectBoundary = {
        type: 'Task',
        alias: `tasks-${tenantId}`, // Using the required alias format
        active: true,
        createdBy: { userId: { systemID: operatorSystemID, email: operatorEmail } },
        status: 'active',
        objectDetails: {
            ...task.details
        }
    };
        
    const response = await api.post('/objects', objectBoundary);
    return response.data;
}

export async function bindTaskToAC(ACSystemID, ACId, taskObjectId, operatorSystemID, operatorEmail) {
    const response = await api.put(
        `/objects/${ACSystemID}/${ACId}/children?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
        taskObjectId
    );
    return response.data;
}

export async function deleteTask({ taskSystemID, taskObjectId, operatorSystemID, operatorEmail }) {

    // Get current task data to preserve required fields
    const currentResponse = await api.get(
        `/objects/${taskSystemID}/${taskObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`
    );

    const currentTask = currentResponse.data;

    const updateData = {
        type: currentTask.type,
        alias: currentTask.alias,
        active: false, // Soft delete
        status: ScheduleConstants.STATUS.INACTIVE, // Mark as inactive
        objectDetails: currentTask.objectDetails,
        createdBy: currentTask.createdBy
    };

    const response = await api.put(
        `/objects/${taskSystemID}/${taskObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
        updateData
    );

    return response.data;
}

export async function updateTask({ taskSystemID, taskObjectId, operatorSystemID, operatorEmail, updates }) {

    const currentResponse = await api.get(
        `/objects/${taskSystemID}/${taskObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`
    );

    const currentTask = currentResponse.data;

    const updateData = {
        type: currentTask.type,
        alias: currentTask.alias, // Keep the original alias
        active: currentTask.active,
        status: updates.status || currentTask.status,
        createdBy: currentTask.createdBy,
        objectDetails: {
            ...currentTask.objectDetails,
            ...updates.details,
            lastModified: new Date().toISOString()
        }
    };

    const response = await api.put(
        `/objects/${taskSystemID}/${taskObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
        updateData
    );

    return response.data;
}

export async function toggleTaskStatus({ taskSystemID, taskObjectId, operatorSystemID, operatorEmail }) {

    // Get current task data
    const currentResponse = await api.get(
        `/objects/${taskSystemID}/${taskObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`
    );

    const currentTask = currentResponse.data;
    const newStatus = currentTask.status === ScheduleConstants.STATUS.ACTIVE ? 
        ScheduleConstants.STATUS.INACTIVE : ScheduleConstants.STATUS.ACTIVE;

    // Update only the status
    const updateData = {
        type: currentTask.type,
        alias: currentTask.alias,
        active: currentTask.active,
        status: newStatus,
        createdBy: currentTask.createdBy,
        objectDetails: {
            ...currentTask.objectDetails,
            lastStatusChange: new Date().toISOString()
        }
    };

    const response = await api.put(
        `/objects/${taskSystemID}/${taskObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
        updateData
    );

    return response.data;
}

// getTasksForTenant by alias "tasks-<tenantId>"
export async function fetchTasksForTenant({
    userSystemID,
    userEmail,
    page = 0,
    size = 10,
    tenantId
}) {
    const response = await api.get(
        `/objects/search/byAlias/tasks-${tenantId}?userSystemID=${userSystemID}&userEmail=${userEmail}&page=${page}&size=${size}`,
    );
    return response.data;
}

export async function getACByTaskId({ taskSystemID, taskObjectId, userSystemID, userEmail }) {
    const response = await api.get(
        `/objects/${taskSystemID}/${taskObjectId}/parents?userSystemID=${userSystemID}&userEmail=${userEmail}`
    );

    const acs = response.data.filter(obj => obj.type === 'AirConditioner' || obj.type === 'AC');
    if (acs.length > 0) {
        return acs[0]; // Return the first AC parent found
    }
    
    throw new Error('No AC parent found for the given task');
}

export default {
    createTask,
    getTasksForAC,
    bindTaskToAC,
    deleteTask,
    updateTask,
    toggleTaskStatus,
    fetchTasksForTenant
}