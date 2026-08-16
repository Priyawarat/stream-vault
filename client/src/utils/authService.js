import { apiClient } from "./apiClient";
import { ENDPOINTS } from "./constants";

export const loginRequest = async ({ email, password }) => {
  const { data } = await apiClient.post(ENDPOINTS.login, { email, password });
  return data?.data;
};

export const registerRequest = async ({ fullName, email, mobile, password }) => {
  const { data } = await apiClient.post(ENDPOINTS.register, { fullName, email, mobile, password });
  return data?.data;
};
