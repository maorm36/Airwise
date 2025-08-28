import api from './api';

export async function getSitesForTenant({ tenantSystemID, tenantId, userSystemID, userEmail, size = 10, page = 0 }) {
  try {
    const response = await api.get(
      `/objects/${tenantSystemID}/${tenantId}/children`,
      { params: { userSystemID, userEmail, size, page } }
    );

    return response.data.filter(obj => obj.type === 'Site');
  } catch (error) {

    if (error.response?.status === 404) {
      return [];
    }

    throw error;
  }
}

export async function createSite({ operatorSystemID, operatorEmail, site }) {
  const objectBoundary = {
    type: 'Site',
    alias: site.name,
    active: true,
    createdBy: { userId: { systemID: operatorSystemID, email: operatorEmail } },
    status: 'completed',
    objectDetails: {
      inSite: true,
      siteName: site.name,
    }
  };
  const response = await api.post('/objects', objectBoundary);
  return response.data;
}

export async function bindSiteToTenant(tenantSystemID, tenantId, siteObjectId, operatorSystemID, operatorEmail) {
  const response = await api.put(
    `/objects/${tenantSystemID}/${tenantId}/children?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
    siteObjectId
  );
  return response.data;
}

export async function deleteSite({ siteSystemID, siteObjectId, operatorSystemID, operatorEmail }) {

  const currentResponse = await api.get(
    `/objects/${siteSystemID}/${siteObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`
  );

  const currentSite = currentResponse.data;
  // Create complete ObjectBoundary with all required fields preserved
  const updateData = {
    type: currentSite.type,
    alias: currentSite.alias,
    active: false, // This is what we want to change
    status: currentSite.status,
    objectDetails: currentSite.objectDetails,
    createdBy: currentSite.createdBy
  };

  const response = await api.put(
    `/objects/${siteSystemID}/${siteObjectId}?userSystemID=${operatorSystemID}&userEmail=${operatorEmail}`,
    updateData
  );

  return response.data;
}

export async function updateSite(systemID, objectId, userSystemID, userEmail, updatedSite) {
  const objectBoundary = {
    ...updatedSite,
  };
  const response = await api.put(
    `/objects/${systemID}/${objectId}?userSystemID=${userSystemID}&userEmail=${userEmail}`,
    objectBoundary
  );
  return response.data;
}


export default {
  getSitesForTenant,
  createSite,
  updateSite,
  bindSiteToTenant,
  deleteSite,
};
