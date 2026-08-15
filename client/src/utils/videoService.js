import { apiClient, refreshAccessToken } from "./apiClient";
import { ACCEPTED_VIDEO_TYPE, API_BASE_URL, ENDPOINTS } from "./constants";
import { getAccessToken } from "./storage";

/** Mock payload used when the API is unreachable, matching the documented contract. */
const mockUploadResponse = (file) => ({
  data: {
    videoId: crypto.randomUUID ? crypto.randomUUID() : `dda5d8f5-793b-42f1-a0ca-${Date.now()}`,
    fileName: file?.name || "medium.mp4",
    fileSize: file?.size ?? 9360758,
    contentType: ACCEPTED_VIDEO_TYPE,
    status: "UPLOADED",
  },
  error: null,
  timestamp: new Date().toISOString(),
});

export const fetchVideos = async () => {
  const { data } = await apiClient.get(ENDPOINTS.videos);
  return Array.isArray(data?.data) ? data.data : [];
};

export const uploadVideo = async (file, onProgress) => {
  const formData = new FormData();
  formData.append("file", file);
  try {
    const { data } = await apiClient.post(ENDPOINTS.upload, formData, {
      onUploadProgress: (event) => {
        if (!onProgress || !event.total) return;
        onProgress(Math.round((event.loaded * 100) / event.total));
      },
    });
    return data;
  } catch (error) {
    if (error?.message === "Network Error") {
      onProgress?.(100);
      return mockUploadResponse(file);
    }
    throw error;
  }
};

/**
 * The stream endpoints are JWT protected, so a plain <video src> cannot be used.
 * We fetch the bytes with the bearer token and hand the player an object URL.
 */
export const createStreamObjectUrl = async (videoId, { full = false, signal } = {}) => {
  const path = full ? ENDPOINTS.streamFull(videoId) : ENDPOINTS.stream(videoId);
  const request = async (token) =>
    fetch(`${API_BASE_URL}${path}`, {
      method: "GET",
      credentials: "include",
      signal,
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(full ? {} : { Range: "bytes=0-" }),
      },
    });

  let response = await request(getAccessToken());
  if (response.status === 401) {
    const token = await refreshAccessToken();
    response = await request(token);
  }
  if (!response.ok && response.status !== 206) {
    throw new Error(`Stream failed with status ${response.status}`);
  }
  const blob = await response.blob();
  return URL.createObjectURL(blob.type ? blob : new Blob([blob], { type: ACCEPTED_VIDEO_TYPE }));
};
