import { ACCESS_TOKEN_KEY } from "./constants";

export const getAccessToken = () => {
  try {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  } catch {
    return null;
  }
};

export const setAccessToken = (token) => {
  try {
    if (token) localStorage.setItem(ACCESS_TOKEN_KEY, token);
  } catch {
    /* storage unavailable */
  }
};

export const clearAccessToken = () => {
  try {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  } catch {
    /* storage unavailable */
  }
};

export const decodeJwt = (token) => {
  if (!token || typeof token !== "string" || token.split(".").length < 2) return null;
  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(decodeURIComponent(escape(window.atob(payload))));
  } catch {
    return null;
  }
};
