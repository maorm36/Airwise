import api from './api';

export async function createUser(newUser) {
  const response = await api.post(
    '/users',
    newUser
  );
  return response.data;
}

export async function loginUser(systemID, email) {
  const response = await api.get(
    `/users/login/${systemID}/${email}`
  );
  return response.data;
}

export async function updateUser({ systemID, email, updatedUser, userSystemID, userEmail }) {
  await api.put(
    `/users/${systemID}/${email}`,
    updatedUser,
    { params: { userSystemID, userEmail } }
  );
}

export default {
  createUser,
  loginUser,
  updateUser
};
