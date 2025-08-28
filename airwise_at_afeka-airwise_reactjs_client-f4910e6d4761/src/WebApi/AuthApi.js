import api from './api';

// Tenant == User
export const registerTenant = ({ email, username, avatar }) =>
  api.post('/users', { email, username, avatar }).then(res => res.data);

export const loginTenant = (systemID, email) =>
  api.get(`/users/login/${systemID}/${email}`).then(res => res.data);

export const updateTenant = (systemID, email, userBoundary) =>
  api.put(`/users/${systemID}/${email}`, userBoundary).then(res => res.data);
