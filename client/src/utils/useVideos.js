import { useCallback, useEffect, useState } from "react";
import { extractErrorMessage } from "./apiClient";
import { fetchVideos } from "./videoService";

export default function useVideos() {
  const [videos, setVideos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setVideos(await fetchVideos());
    } catch (requestError) {
      setError(extractErrorMessage(requestError, "Unable to load your video library."));
      setVideos([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return { videos, loading, error, reload: load, setVideos };
}
