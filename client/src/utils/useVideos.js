import { useCallback, useEffect, useState } from "react";
import { extractErrorMessage } from "./apiClient";
import {fetchThumbnail, fetchVideos} from "./videoService";

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

export function useVideoThumbnail(id){
  const [thumbnail, setThumbnail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const thumbnailCallBack = useCallback(async () =>{
    setLoading(true);
    setError("");
    try{
      setThumbnail(await fetchThumbnail(id))
    } catch(requestError){
      setError(extractErrorMessage(requestError, "Unable to load thumbnail."))
    } finally {
      setLoading(false);
    }
  },[id])

  useEffect(() => {
    thumbnailCallBack();
  }, [thumbnailCallBack]);

  return { thumbnail, loading, error, reload: thumbnailCallBack, setThumbnail };
}
