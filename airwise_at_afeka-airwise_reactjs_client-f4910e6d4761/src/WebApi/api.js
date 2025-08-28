import axios from 'axios';
import apiConfig from './apiConfig.json';

const config = {
  ...apiConfig,
  baseURL: apiConfig.baseURL,
};

const api = axios.create(config);

api.interceptors.response.use(
  response => response,
  error => {
    // Don't show alerts for 404 errors - these are often expected
    // (like when fetching children that don't exist)
    if (error.response?.status === 404) {
      // Just log it and let the calling code handle it
      return Promise.reject(error);
    }

    // Only show alerts for actual errors (5xx, network errors, etc.)
    const message =
      error.response?.data?.message ||
      error.response?.data ||
      error.message;

    // Show alert for real errors
    if (error.response?.status >= 500 || !error.response) {
      alert(message); // Only show for server errors or network issues
    }

    return Promise.reject(error);
  }
);

export default api;