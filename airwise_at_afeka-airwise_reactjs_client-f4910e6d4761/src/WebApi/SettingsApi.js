import api from './api';
import * as defaultSettings from '../defaultSettings.json';


export async function createSettings(operatorSystemID, operatorEmail, tenantId) {
    const objectBoundary = {
        type: 'Settings',
        alias: `Settings-${tenantId}`,
        active: true,
        status: 'completed',
        createdBy: { userId: { systemID: operatorSystemID, email: operatorEmail } },
        objectDetails: {
            ...defaultSettings
        }
    };
    const response = await api.post(
        '/objects',
        objectBoundary
    );
    return response.data;
}


export async function updateSettings(objectSystemID, objectId, updatedSettings, operatorSystemID, operatorEmail, tenantId) {
    const objectBoundary = {
        type: 'Settings',
        alias: `Settings-${tenantId}`,
        active: true,
        status: 'completed',
        createdBy: { userId: { systemID: operatorSystemID, email: operatorEmail } },
        objectDetails: {
            ...updatedSettings
        }
    };
    const response = await api.put(
        `/objects/${objectSystemID}/${objectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
        objectBoundary
    );
    return response.data;
}

export async function getSettings({
    userSystemID,
    userEmail,
    page = 0,
    size = 1,
    tenantId
}) {
    const response = await api.get(
        `/objects/search/byAlias/Settings-${tenantId}`,
        {
            params: { userSystemID, userEmail, page, size }
        }
    );
    return response.data;
}


export async function getMySettings({ userSystemID, userEmail, tenantId }) {
    const list = await getSettings({ userSystemID, userEmail, page: 0, size: 1, tenantId: tenantId });
    return list.length ? list[0] : null;
}

export default {
    createSettings,
    getMySettings,
    updateSettings,
    // bindSettingsToTenant
};