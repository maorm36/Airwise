import api from './api';

export async function getNotificationsForTenant({ tenantSystemID, tenantId, userSystemID, userEmail, size = 8, page = 0 }) {
  const response = await api.get(
        `/objects/search/byAliasPattern/notification-${tenantId}?userSystemID=${userSystemID}&userEmail=${userEmail}&page=${page}&size=${size}`,
    );
  return response.data.filter(obj => obj.type === 'Notification');
};  

export default {
  getNotificationsForTenant,
};