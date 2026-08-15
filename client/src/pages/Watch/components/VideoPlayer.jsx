import { useEffect, useRef, useState } from "react";
import Alert from "../../../utils/Alert";
import Spinner from "../../../utils/Spinner";
import { createStreamObjectUrl } from "../../../utils/videoService";
import { extractErrorMessage } from "../../../utils/apiClient";

export default function VideoPlayer({ videoId, mode }) {
  const videoRef = useRef(null);
  const [src, setSrc] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!videoId) return undefined;
    const controller = new AbortController();
    let objectUrl = "";
    setLoading(true);
    setError("");
    setSrc("");

    createStreamObjectUrl(videoId, { full: mode === "full", signal: controller.signal })
      .then((url) => {
        objectUrl = url;
        setSrc(url);
      })
      .catch((streamError) => {
        if (streamError?.name === "AbortError") return;
        setError(extractErrorMessage(streamError, "Unable to load this video stream."));
      })
      .finally(() => setLoading(false));

    return () => {
      controller.abort();
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [videoId, mode]);

  return (
    <div className="sv-card overflow-hidden">
      <div className="relative aspect-video bg-black">
        {loading && (
          <div className="absolute inset-0 grid place-items-center text-vault-brand">
            <Spinner size={30} />
          </div>
        )}
        {!loading && !error && src && (
          <video
            ref={videoRef}
            src={src}
            controls
            playsInline
            preload="metadata"
            poster="/images/video-poster.svg"
            className="h-full w-full"
          />
        )}
        {!loading && error && (
          <div className="absolute inset-0 grid place-items-center p-6">
            <Alert tone="error">{error}</Alert>
          </div>
        )}
      </div>
    </div>
  );
}
