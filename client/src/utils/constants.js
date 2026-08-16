export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/v1";

export const ENDPOINTS = {
  register: "/users/register",
  login: "/users/login",
  refreshToken: "/users/refresh-token",
  videos: "/videos",
  upload: "/videos/upload",
  stream: (id) => `/videos/${id}/stream`,
  streamFull: (id) => `/videos/${id}/stream`,
  thumbnail: (id) => `/videos/${id}/thumbnail`,
  allVariants: (id) => `/videos/${id}/variants`,
  variant: (id, variant) => `/videos/${id}/variants/${variant}/stream`,
};

export const ACCESS_TOKEN_KEY = "sv_access_token";
export const ACCEPTED_VIDEO_TYPE = "video/mp4";
export const MAX_UPLOAD_BYTES = 512 * 1024 * 1024;

export const VIDEO_STATUS = {
  UPLOADED: { label: "Uploaded", className: "bg-vault-brand2/15 text-vault-brand2" },
  PROCESSING: { label: "Processing", className: "bg-amber-400/15 text-amber-300" },
  READY: { label: "Ready", className: "bg-vault-brand/15 text-vault-brand" },
  FAILED: { label: "Failed", className: "bg-vault-danger/15 text-vault-danger" },
};
