import axios from "axios";
import { API_BASE_URL, ENDPOINTS } from "./constants";
import { clearAccessToken, getAccessToken, setAccessToken } from "./storage";

/**
 * Shared axios instance.
 * withCredentials keeps the httpOnly refreshToken cookie flowing to the API.
 */
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});

let refreshPromise = null;
const onUnauthorized = [];
export const registerUnauthorizedHandler = (handler) => {
  onUnauthorized.push(handler);
  return () => {
    const index = onUnauthorized.indexOf(handler);
    if (index > -1) onUnauthorized.splice(index, 1);
  };
};

export const refreshAccessToken = async () => {
  if (!refreshPromise) {
    refreshPromise = axios
      .post(`${API_BASE_URL}${ENDPOINTS.refreshToken}`, {}, { withCredentials: true })
      .then((response) => {
        const token = response?.data?.data?.accessToken;
        if (!token) throw new Error("No access token in refresh response");
        setAccessToken(token);
        return token;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
};

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  if (config.data instanceof FormData) delete config.headers["Content-Type"];
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config || {};
    const status = error.response?.status;
    const isRefreshCall = String(original.url || "").includes(ENDPOINTS.refreshToken);

    if (status === 401 && !original._retried && !isRefreshCall) {
      original._retried = true;
      try {
        const token = await refreshAccessToken();
        original.headers = { ...(original.headers || {}), Authorization: `Bearer ${token}` };
        return apiClient(original);
      } catch (refreshError) {
        clearAccessToken();
        onUnauthorized.forEach((handler) => handler());
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  },
);

export const extractErrorMessage = (error, fallback = "Something went wrong. Please try again.") => {
  const apiError = error?.response?.data?.error;
  if (typeof apiError === "string" && apiError.trim()) return apiError;
  if (apiError?.message) return apiError.message;
  if (error?.response?.data?.message) return error.response.data.message;
  if (error?.message === "Network Error") return "Cannot reach the StreamVault API. Is the server running?";
  return error?.message || fallback;
};
